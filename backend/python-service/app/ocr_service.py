from google.cloud import vision
import time
import logging

# Cấu hình logging
logging.basicConfig(level=logging.INFO)

def recognize_text_with_google_vision(image_path):
    """
    Nhận diện văn bản sử dụng Google Vision API.
    """
    try:
        start_time = time.time()
        client = vision.ImageAnnotatorClient()

        with open(image_path, "rb") as image_file:
            content = image_file.read()

        image = vision.Image(content=content)
        response = client.text_detection(image=image)
        texts = response.text_annotations

        extracted_text = texts[0].description if texts else ""
        confidence = texts[0].score if texts and hasattr(texts[0], "score") else 0
        processing_time = round((time.time() - start_time) * 1000)

        return {
            "library": "Google Vision API",
            "text": extracted_text.strip(),
            "confidence": f"{confidence * 100:.2f}%" if confidence else "N/A",
            "time": f"{processing_time} ms",
        }
    except Exception as e:
        logging.error(f"Error in Google Vision API: {str(e)}")
        return {"library": "Google Vision API", "text": f"Error: {str(e)}"}
