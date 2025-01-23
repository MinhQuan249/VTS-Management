import uuid
import logging
from pdf2image import convert_from_path

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
