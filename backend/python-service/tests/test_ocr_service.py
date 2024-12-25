import requests

# Địa chỉ API Flask
URL = "http://127.0.0.1:5000/ocr"

def test_ocr_service():
    with open("C:\Users\Admin\OneDrive - MSFT\Pictures\Screenshots\Screenshot 2024-03-06 003126.png", "rb") as file:
        files = {"file": file}
        response = requests.post(URL, files=files)
        print(response.json())

if __name__ == "__main__":
    test_ocr_service()
s