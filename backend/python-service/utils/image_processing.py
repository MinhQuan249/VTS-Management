import cv2
import os
import uuid
import logging
from pdf2image import convert_from_path
import numpy as np

# Cấu hình logging
logging.basicConfig(level=logging.INFO)

def convert_pdf_to_images(pdf_path):
    """
    Chuyển đổi PDF thành danh sách ảnh.
    """
    try:
        images = convert_from_path(pdf_path)
        image_paths = []
        for i, image in enumerate(images):
            output_path = f"temp/page_{i}_{uuid.uuid4().hex}.png"
            image.save(output_path, "PNG")
            image_paths.append(output_path)
        return image_paths
    except Exception as e:
        logging.error(f"Error converting PDF to images: {str(e)}")
        return []

def cleanup_temp_files(file_paths):
    """
    Xóa các file tạm.
    """
    for file_path in file_paths:
        if os.path.exists(file_path):
            os.remove(file_path)

def preprocess_image(image_path):
    """
    Tiền xử lý ảnh:
    - Chuyển ảnh thành đen trắng (adaptive threshold).
    - Làm sạch nhiễu nhỏ (morphology).
    - Làm sắc nét nhẹ và lưu kết quả với tên file tạm thời.
    """
    try:
        img = cv2.imread(image_path, cv2.IMREAD_COLOR)
        img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        img_adaptive_thresh = cv2.adaptiveThreshold(
            img_gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 15, 10
        )

        kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (2, 2))
        img_cleaned = cv2.morphologyEx(img_adaptive_thresh, cv2.MORPH_OPEN, kernel)

        kernel_sharpen = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]])
        img_sharpen = cv2.filter2D(img_cleaned, -1, kernel_sharpen)

        output_path = f"temp/processed_{uuid.uuid4().hex}.png"
        cv2.imwrite(output_path, img_sharpen)
        return output_path
    except Exception as e:
        logging.error(f"Error preprocessing image: {str(e)}")
        return None
