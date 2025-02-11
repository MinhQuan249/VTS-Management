import logging
import os
import re
import subprocess
import time
from difflib import SequenceMatcher
from typing import Dict, List

import cv2
import numpy as np
import unicodedata
from PIL import Image
from docx import Document
from skimage.measure import shannon_entropy

# Cấu hình logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
#
# Đường dẫn Tesseract (Windows)
# pytesseract.pytesseract.tesseract_cmd = "/usr/local/bin/tesseract"

def analyze_image(image_path):
    # Đọc ảnh
    image = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
    if image is None:
        raise ValueError("Cannot read image.")
    height, width = image.shape

    analysis = {}

    # 1. Phân tích độ phân giải
    image_pil = Image.open(image_path)
    dpi = image_pil.info.get('dpi', (72, 72))[0]
    analysis["resolution"] = f"{dpi} dpi"
    analysis["needs_rescale"] = dpi < 300

    # 2. Phân tích độ nhiễu
    entropy = shannon_entropy(image)
    analysis["noise_level"] = "High" if entropy < 5 else "Low"

    # 3. Phân tích góc nghiêng
    edges = cv2.Canny(image, 50, 150)  # Phát hiện cạnh
    lines = cv2.HoughLines(edges, 1, np.pi / 180, 200)
    skew_angle = 0  # Mặc định là không nghiêng

    if lines is not None:
        angles = []
        for rho, theta in lines[:, 0]:
            angle = np.degrees(theta) - 90
            angles.append(angle)
        skew_angle = np.median(angles)  # Góc nghiêng trung vị
    analysis["skew_angle"] = f"{skew_angle:.2f} degrees"
    analysis["is_skewed"] = abs(skew_angle) > 2  # Nghiêng nếu góc > 2 độ

    # 4. Phân tích mức độ dính ký tự
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (1, 2))
    eroded = cv2.erode(image, kernel, iterations=1)
    analysis["character_spacing"] = "Tight" if np.sum(eroded != image) > 1000 else "Normal"

    # 5. Độ tương phản
    contrast = np.std(image)
    analysis["contrast_level"] = "Low" if contrast < 50 else "Good"

    # 7. Tỉ lệ văn bản
    text_area = np.sum(image < 128) / (height * width)  # Vùng đen được coi là văn bản
    analysis["text_coverage"] = f"{text_area * 100:.2f}%"

    # 8. Định dạng file
    file_format = image_path.split(".")[-1].upper()
    analysis["file_format"] = file_format
    analysis["format_recommended"] = "TIFF" if file_format != "TIFF" else "Good"

    # Tổng hợp gợi ý xử lý
    analysis["recommendations"] = []
    if analysis["needs_rescale"]:
        analysis["recommendations"].append("Rescale image to at least 300 dpi using image editing software or AI tools.")
    if analysis["noise_level"] == "High":
        analysis["recommendations"].append("Apply noise reduction using filters like Median or Gaussian Blur.")
    if analysis["contrast_level"] == "Low":
        analysis["recommendations"].append("Enhance contrast using Histogram Equalization or CLAHE.")
    if analysis["character_spacing"] == "Tight":
        analysis["recommendations"].append("Separate characters using dilation or morphological operations.")
    if analysis["is_skewed"]:
        analysis["recommendations"].append(f"Deskew the image by rotating it {skew_angle:.2f} degrees.")
    if analysis["text_coverage"] and float(analysis["text_coverage"].strip('%')) < 20:
        analysis["recommendations"].append("Crop the image to focus on the text region.")
    if analysis["file_format"] != "TIFF":
        analysis["recommendations"].append("Convert the file to TIFF for better OCR performance.")

    for key, value in analysis.items():
        if isinstance(value, list):
            logger.info(f"{key}:")
            for item in value:
                logger.info(f"  - {item}")
        else:
            logger.info(f"{key}: {value}")

    return analysis

def recognize_text_with_tesseract(image_path):
     try:
         logger.info("Starting Tesseract OCR for file: %s", image_path)
         start_time = time.time()

         # Gọi CLI của Tesseract
         output_file = "/tmp/tesseract_output"  # Tên file tạm để lưu kết quả OCR
         command = f"/usr/local/bin/tesseract {image_path} {output_file} -l Vietnamese"
         subprocess.run(command, shell=True, check=True)

         # Đọc kết quả từ file đầu ra
         with open(f"{output_file}.txt", "r", encoding="utf-8") as file:
#              text = file.read().strip().lower()
             text = file.read().strip()

         processing_time = round((time.time() - start_time) * 1000)
         logger.info("OCR completed in %d ms", processing_time)

         return {"text": text, "time": f"{processing_time} ms"}
     except Exception as e:
         logger.error(f"Error in OCR processing: {str(e)}")
         return {"text": f"Error: {str(e)}", "time": "N/A"}

def process_word_to_text(file_path):
    try:
        logger.info("Processing Word file: %s", file_path)
        if file_path.endswith(".docx"):
            return convert_docx_to_text(file_path)
        elif file_path.endswith(".doc"):
            return convert_doc_to_text(file_path)
        else:
            raise ValueError("Unsupported Word file format.")
    except Exception as e:
        logger.error(f"Error processing Word file: {str(e)}")
        return f"Error: {str(e)}"

def convert_docx_to_text(docx_path):
    try:
        logger.info("Converting .docx to text: %s", docx_path)
        doc = Document(docx_path)
        full_text = "\n".join(paragraph.text for paragraph in doc.paragraphs)
        return full_text.strip()
    except Exception as e:
        logger.error(f"Error converting .docx to text: {str(e)}")
        return f"Error: {str(e)}"

def convert_doc_to_text(doc_path):
    try:
        logger.info("Converting .doc to text using Antiword: %s", doc_path)
        command = f"antiword {doc_path}"
        result = subprocess.run(command, shell=True, text=True, capture_output=True)

        if result.returncode != 0:
            raise Exception(f"Antiword failed: {result.stderr}")

        return result.stdout.strip()
    except Exception as e:
        logger.error(f"Error converting .doc to text: {str(e)}")
        return f"Error: {str(e)}"

def find_common_text(ocr_text: str, db_text: str, threshold: int = 3) -> List[str]:
    # Chuẩn hóa văn bản
    ocr_text = re.sub(r'\s+', ' ', ocr_text.strip())
    db_text = re.sub(r'\s+', ' ', db_text.strip())

    ocr_words = ocr_text.split()
    db_words = db_text.split()
    matcher = SequenceMatcher(None, ocr_words, db_words)
    matches = []

    for match in matcher.get_matching_blocks():
        if match.size > 0:
            common = ocr_words[match.a: match.a + match.size]
            common_text = " ".join(common)
            # Lọc đoạn trùng lặp theo ngưỡng số từ
            if len(common_text.split()) >= threshold:
                matches.append(common_text)
    return matches

def preprocess_text(text: str) -> str:
    text = unicodedata.normalize("NFKC", text)
    text = re.sub(r'\s+', ' ', text)  # Loại bỏ khoảng trắng thừa
    text = re.sub(r'[^\w\s]', '', text)  # Loại bỏ ký tự đặc biệt
    return text.lower().strip()

def calculate_jaccard_similarity(text1: str, text2: str) -> float:
    """
    Tính toán độ tương đồng Jaccard giữa hai văn bản.
    :param text1: Văn bản đầu tiên.
    :param text2: Văn bản thứ hai.
    :return: Jaccard Similarity (giá trị giữa 0 và 1).
    """
    set1 = set(text1.split())
    set2 = set(text2.split())
    intersection = len(set1.intersection(set2))
    union = len(set1.union(set2))
    return intersection / union if union > 0 else 0.0

def calculate_similarity(ocr_text: str, database_texts: List[Dict[str, str]]) -> List[Dict[str, float]]:
    try:
        logger.info("Calculating similarity for OCR text with %d documents.", len(database_texts))

        if not ocr_text or not database_texts:
            raise ValueError("OCR text or database texts cannot be empty.")
        if len(ocr_text.split()) < 5:
            raise ValueError("OCR text is too short for reliable comparison.")

        ocr_text = preprocess_text(ocr_text)
        if not ocr_text:
            raise ValueError("Processed OCR text is empty after preprocessing.")

        database_texts = [
            {"id": doc['id'], "text": preprocess_text(doc['text']), "fileName": doc.get('fileName', 'Unknown')}
            for doc in database_texts if len(doc['text'].split()) > 5
        ]
        if not database_texts:
            raise ValueError("No valid database texts available for comparison.")

        results = []
        for doc in database_texts:
            jaccard_score = calculate_jaccard_similarity(ocr_text, doc['text'])
            common_texts = find_common_text(ocr_text, doc['text'])
            results.append({
                "document_id": doc['id'],
                "fileName": doc['fileName'],
                "jaccard_similarity": jaccard_score,
                "common_texts": common_texts
            })

        results.sort(key=lambda x: x['jaccard_similarity'], reverse=True)
        logger.info("Similarity calculation completed.")
        return results
    except FileNotFoundError as fe:
        logger.error(f"File error: {fe}")
        raise
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        raise
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        raise

def handle_compare_request(request_data: Dict):
    """
    Xử lý yêu cầu so sánh từ frontend, kiểm tra dữ liệu và gọi hàm so sánh.
    :param request_data: Dữ liệu JSON từ frontend.
    :return: Kết quả so sánh hoặc lỗi.
    """
    ocr_text = request_data.get('text', '').strip()
    documents = request_data.get('documents', [])

    if not ocr_text or not documents:
        logger.error("Missing required fields: 'text' or 'documents'")
        return {"error": "Missing required fields: 'text' or 'documents'"}, 400

    try:
        logger.info("Starting comparison for OCR text with %d documents.", len(documents))
        results = calculate_similarity(ocr_text, documents)
        return {"results": results}, 200
    except ValueError as ve:
        logger.warning(f"Validation error during comparison: {ve}")
        return {"error": str(ve)}, 400
    except Exception as e:
        logger.error(f"Error during comparison: {str(e)}")
        return {"error": "An unexpected error occurred during comparison."}, 500
