from flask import Flask, request, jsonify
from utils.image_processing import convert_pdf_to_images, preprocess_image
from ocr_service import recognize_text_with_google_vision, process_word_to_text, handle_compare_request  # Import hàm so sánh
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
    file_path = None
    image_paths = []
    try:
        if 'file' not in request.files:
            logger.error("No file uploaded in the request.")
            return jsonify({"error": "No file uploaded"}), 400

        file = request.files['file']
        file_path, file_ext = save_uploaded_file(file)

        start_time = time.time()

        if file_ext in ['.doc', '.docx']:
            logger.info("Processing Word file...")
            extracted_text = process_word_to_text(file_path)
            results = [{
                "file": os.path.basename(file_path),
                "text": extracted_text,
                "time": f"{time.time() - start_time:.2f} seconds"
            }]
            return jsonify({"results": results}), 200

        elif file_ext == '.pdf':
            logger.info("Processing PDF file...")
            results = []
            # Sử dụng hình ảnh trực tiếp, không qua tiền xử lý
            image_paths = convert_pdf_to_images(file_path)
            for image_path in image_paths:
                try:
                    # Comment tiền xử lý
                    # processed_image_path = preprocess_image(image_path)
                    # result = recognize_text_with_google_vision(processed_image_path or image_path)

                    # Xử lý trực tiếp
                    result = recognize_text_with_google_vision(image_path)
                    results.append({
                        "image": os.path.basename(image_path),
                        "text": result.get('text', ''),
                        "time": f"{time.time() - start_time:.2f} seconds"
                    })
                except Exception as e:
                    logger.error(f"Error processing image {image_path}: {e}")
                    results.append({"image": os.path.basename(image_path), "error": str(e)})
            return jsonify({"results": results}), 200

        elif file_ext in ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif', '.webp']:
            logger.info("Processing image file...")
            # Comment tiền xử lý
            # processed_image_path = preprocess_image(file_path)
            # result = recognize_text_with_google_vision(processed_image_path or file_path)

            # Xử lý trực tiếp
            result = recognize_text_with_google_vision(file_path)
            results = [{
                "image": os.path.basename(file_path),
                "text": result.get('text', ''),
                "time": f"{time.time() - start_time:.2f} seconds"
            }]
            return jsonify({"results": results}), 200

        else:
            logger.error(f"Unsupported file type: {file_ext}")
            return jsonify({"error": f"Unsupported file type: {file_ext}"}), 400

    except Exception as e:
        logger.error(f"Error during OCR processing: {e}")
        logger.error(traceback.format_exc())
        return jsonify({"error": str(e)}), 500

    finally:
        clean_temp_files(file_path, *image_paths)

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

        # Thêm thông tin hiển thị đoạn trùng lặp
        for result in results.get('results', []):
            common_texts = result.get("common_texts", [])
            result['display_common_texts'] = common_texts[:5]  # Hiển thị tối đa 5 đoạn trùng lặp

        return jsonify(results), status_code
    except Exception as e:
        logger.error(f"Error during text comparison: {e}")
        logger.error(traceback.format_exc())
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)