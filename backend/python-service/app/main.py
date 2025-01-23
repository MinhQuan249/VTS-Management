from flask import Flask, request, jsonify
from utils.image_processing import convert_pdf_to_images
from ocr_service import recognize_text_with_tesseract, process_word_to_text, handle_compare_request
import os
import uuid
import traceback
import logging
import time

# Cấu hình logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

TEMP_DIR = "temp"

def save_uploaded_file(upload_file):
    file_ext = os.path.splitext(upload_file.filename)[1].lower()
    file_path = os.path.join(TEMP_DIR, f"temp_{uuid.uuid4().hex}{file_ext}")
    os.makedirs(TEMP_DIR, exist_ok=True)
    upload_file.save(file_path)
    logger.info(f"File uploaded and saved to {file_path}")
    return file_path, file_ext

def clean_temp_files(*file_paths):
    for file_path in file_paths:
        if file_path and os.path.exists(file_path):
            os.remove(file_path)
            logger.info(f"Temporary file {file_path} deleted.")
@app.route('/ocr/upload', methods=['POST'])
def ocr_service():
    uploaded_files = request.files.getlist('files')  # Lấy danh sách các file từ request
    if not uploaded_files:
        logger.error("No files uploaded in the request.")
        return jsonify({"error": "No files uploaded"}), 400

    results = []  # Danh sách lưu kết quả OCR cho từng file
    for file in uploaded_files:
        file_path = None
        image_paths = []
        try:
            file_path, file_ext = save_uploaded_file(file)
            start_time = time.time()

            if file_ext in ['.doc', '.docx']:
                logger.info("Processing Word file...")
                extracted_text = process_word_to_text(file_path)
                results.append({
                    "file": os.path.basename(file_path),
                    "text": extracted_text,
                    "time": f"{time.time() - start_time:.2f} seconds"
                })

            elif file_ext == '.pdf':
                logger.info("Processing PDF file...")
                image_paths = convert_pdf_to_images(file_path)
                for image_path in image_paths:
                    try:
                        result = recognize_text_with_tesseract(image_path)
                        results.append({
                            "image": os.path.basename(image_path),
                            "text": result.get('text', ''),
                            "time": f"{time.time() - start_time:.2f} seconds"
                        })
                    except Exception as e:
                        logger.error(f"Error processing image {image_path}: {e}")
                        results.append({"image": os.path.basename(image_path), "error": str(e)})

            elif file_ext in ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif', '.webp']:
                logger.info("Processing image file...")
                result = recognize_text_with_tesseract(file_path)
                results.append({
                    "image": os.path.basename(file_path),
                    "text": result.get('text', ''),
                    "time": f"{time.time() - start_time:.2f} seconds"
                })

            else:
                logger.error(f"Unsupported file type: {file_ext}")
                results.append({"file": os.path.basename(file.filename), "error": f"Unsupported file type: {file_ext}"})

        except Exception as e:
            logger.error(f"Error processing file {file.filename}: {e}")
            logger.error(traceback.format_exc())
            results.append({"file": os.path.basename(file.filename), "error": str(e)})

        finally:
            clean_temp_files(file_path, *image_paths)

    return jsonify({"results": results}), 200

@app.route('/ocr/compare', methods=['POST'])
def compare_service():
    """
    API so sánh văn bản OCR với các văn bản trong cơ sở dữ liệu.
    """
    try:
        request_data = request.get_json()
        if not request_data:
            logger.error("No data received for comparison.")
            return jsonify({"error": "No data received for comparison"}), 400

        logger.info("Received comparison request with OCR text and documents.")
        results, status_code = handle_compare_request(request_data)

        return jsonify(results), status_code
    except Exception as e:
        logger.error(f"Error during text comparison: {e}")
        logger.error(traceback.format_exc())
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)