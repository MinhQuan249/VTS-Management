from google.cloud import vision
import time
import logging
import subprocess
from docx import Document
from sentence_transformers import SentenceTransformer, util
from typing import List, Dict
import unicodedata
import re
from difflib import SequenceMatcher

# Cấu hình logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def recognize_text_with_google_vision(image_path):
    try:
        logger.info("Starting Google Vision OCR for file: %s", image_path)
        start_time = time.time()

        client = vision.ImageAnnotatorClient()
        with open(image_path, "rb") as image_file:
            content = image_file.read()

        image = vision.Image(content=content)
        response = client.text_detection(image=image)
        texts = response.text_annotations

        extracted_text = texts[0].description if texts else ""
        processing_time = round((time.time() - start_time) * 1000)
        logger.info("OCR completed in %d ms", processing_time)

        return {"text": extracted_text.strip(), "time": f"{processing_time} ms"}
    except Exception as e:
        logger.error(f"Error in Google Vision API: {str(e)}")
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

model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2') # Load model tại cấp module
def find_common_text(ocr_text: str, db_text: str, threshold: float = 0.6) -> List[str]:
    ocr_words = ocr_text.split()
    db_words = db_text.split()
    matcher = SequenceMatcher(None, ocr_words, db_words)
    matches = []

    for match in matcher.get_matching_blocks():
        if match.size > 0:
            common = ocr_words[match.a: match.a + match.size]
            common_text = " ".join(common)
            if len(common_text.split()) > 2:  # Chỉ giữ các đoạn có từ 3 từ trở lên
                matches.append(common_text)
    return matches


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

        # Chuẩn hóa văn bản đầu vào
        def preprocess_text(text: str) -> str:
            text = unicodedata.normalize("NFKC", text)
            text = re.sub(r'\s+', ' ', text)  # Loại bỏ khoảng trắng thừa
            text = re.sub(r'[^\w\s]', '', text)  # Loại bỏ ký tự đặc biệt
            return text.lower().strip()

        ocr_text = preprocess_text(ocr_text)
        if not ocr_text:
            raise ValueError("Processed OCR text is empty after preprocessing.")

        for doc in database_texts:
            doc['text'] = preprocess_text(doc['text'])

        # Loại bỏ các văn bản ngắn khỏi cơ sở dữ liệu
        database_texts = [doc for doc in database_texts if len(doc['text'].split()) > 5]
        if not database_texts:
            raise ValueError("No valid database texts available for comparison.")

        logger.info("Encoding texts in batches...")
        texts = [ocr_text] + [doc['text'] for doc in database_texts]
        embeddings = model.encode(texts, convert_to_tensor=True)

        ocr_embedding = embeddings[0]
        db_embeddings = embeddings[1:]

        logger.info("Calculating cosine similarity...")
        cosine_similarity_scores = util.pytorch_cos_sim(ocr_embedding, db_embeddings)[0]

        # Tạo danh sách kết quả
        results = []
        for doc, cosine_score in zip(database_texts, cosine_similarity_scores):
            jaccard_score = calculate_jaccard_similarity(ocr_text, doc['text'])
            common_texts = find_common_text(ocr_text, doc['text'])
            results.append({
                "document_id": doc['id'],
                "fileName": doc.get('fileName', 'Unknown'),  # Bổ sung tên file (nếu có)
                "cosine_similarity": float(cosine_score),
                "jaccard_similarity": jaccard_score,
                "common_texts": common_texts
            })

        # Sắp xếp kết quả theo Cosine Similarity
        results.sort(key=lambda x: x['cosine_similarity'], reverse=True)
        logger.info("Similarity calculation completed.")
        return results

    except Exception as e:
        logger.error(f"Error calculating similarity: {str(e)}")
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
