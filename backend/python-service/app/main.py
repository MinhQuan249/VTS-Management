from flask import Flask, request, jsonify
from utils.image_processing import convert_pdf_to_images, preprocess_image, cleanup_temp_files
from ocr_service import recognize_text_with_google_vision
import os
import uuid
import traceback
import logging

# Cấu hình logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

def calculate_cer(ground_truth, recognized_text):
    """
    Tính CER (Character Error Rate).
    """
    total_characters = len(ground_truth)
    errors = sum(1 for gt, ocr in zip(ground_truth, recognized_text) if gt != ocr)
    errors += abs(len(ground_truth) - len(recognized_text))  # Chênh lệch độ dài
    return errors / total_characters if total_characters > 0 else 1.0

def calculate_wer(ground_truth, recognized_text):
    """
    Tính WER (Word Error Rate).
    """
    ground_words = ground_truth.split()
    recognized_words = recognized_text.split()

    total_words = len(ground_words)
    substitutions = sum(1 for gw, rw in zip(ground_words, recognized_words) if gw != rw)
    deletions = abs(len(ground_words) - len(recognized_words))

    return (substitutions + deletions) / total_words if total_words > 0 else 1.0

@app.route('/ocr/upload', methods=['POST'])
def ocr_service():
    """
    API nhận file PDF hoặc ảnh và trả về văn bản nhận diện được.
    """
    file_path = None
    image_paths = []
    try:
        if 'file' not in request.files:
            logger.error("No file uploaded in the request.")
            return jsonify({"error": "No file uploaded"}), 400

        file = request.files['file']
        ground_truth = request.form.get('ground_truth', '').strip()

        # Tạo đường dẫn tạm để lưu file upload
        file_ext = os.path.splitext(file.filename)[1].lower()
        file_path = f"temp/temp_{uuid.uuid4().hex}{file_ext}"
        os.makedirs("temp", exist_ok=True)  # Đảm bảo thư mục tạm tồn tại
        file.save(file_path)
        logger.info(f"File uploaded and saved to {file_path}")

        results = []

        if file_ext == '.pdf':
            # Xử lý file PDF
            logger.info("Processing PDF file...")
            image_paths = convert_pdf_to_images(file_path)
            for image_path in image_paths:
                processed_image_path = preprocess_image(image_path)
                result = recognize_text_with_google_vision(processed_image_path or image_path)

                # Tính CER và WER nếu có ground_truth
                if ground_truth:
                    cer = calculate_cer(ground_truth, result.get('text', ''))
                    wer = calculate_wer(ground_truth, result.get('text', ''))
                    result.update({
                        "cer_accuracy": f"{(1 - cer) * 100:.2f}%",
                        "cer": cer,
                        "wer_accuracy": f"{(1 - wer) * 100:.2f}%",
                        "wer": wer
                    })

                results.append({
                    "page": os.path.basename(image_path),
                    "library": "Google Vision API",
                    "text": result.get('text', ''),
                    "confidence": result.get('confidence', 'N/A'),
                    "time": result.get('time', 'N/A'),
                    "cer_accuracy": result.get('cer_accuracy', "N/A"),
                    "wer_accuracy": result.get('wer_accuracy', "N/A"),
                    "handwritingSupport": "Tốt",
                    "vietnameseSupport": "Có"
                })
        elif file_ext in ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif', '.webp']:
            # Xử lý ảnh đơn
            logger.info("Processing image file...")
            processed_image_path = preprocess_image(file_path)
            result = recognize_text_with_google_vision(processed_image_path or file_path)

            # Tính CER và WER nếu có ground_truth
            if ground_truth:
                cer = calculate_cer(ground_truth, result.get('text', ''))
                wer = calculate_wer(ground_truth, result.get('text', ''))
                result.update({
                    "cer_accuracy": f"{(1 - cer) * 100:.2f}%",
                    "cer": cer,
                    "wer_accuracy": f"{(1 - wer) * 100:.2f}%",
                    "wer": wer
                })

            results.append({
                "image": os.path.basename(file_path),
                "library": "Google Vision API",
                "text": result.get('text', ''),
                "confidence": result.get('confidence', 'N/A'),
                "time": result.get('time', 'N/A'),
                "cer_accuracy": result.get('cer_accuracy', "N/A"),
                "wer_accuracy": result.get('wer_accuracy', "N/A"),
                "handwritingSupport": "Tốt",
                "vietnameseSupport": "Có"
            })
        else:
            logger.error(f"Unsupported file type: {file_ext}")
            return jsonify({"error": f"Unsupported file type: {file_ext}"}), 400

        return jsonify({"results": results}), 200

    except Exception as e:
        logger.error(f"Error during OCR processing: {e}")
        logger.error(traceback.format_exc())
        return jsonify({"error": str(e)}), 500

    finally:
        # Dọn dẹp file tạm
        if file_path and os.path.exists(file_path):
            os.remove(file_path)
            logger.info(f"Temporary file {file_path} deleted.")
        for image_path in image_paths:
            if os.path.exists(image_path):
                os.remove(image_path)
                logger.info(f"Temporary image file {image_path} deleted.")

if __name__ == '__main__':
    # Khởi chạy Flask server
    app.run(debug=True, host="0.0.0.0", port=5000)
