VTS-Management

🌟 Mục Tiêu Dự Án
Dự án VTS-Management được xây dựng nhằm cung cấp giải pháp nhận diện văn bản (OCR) từ các file ảnh và PDF, đồng thời tích hợp quản lý hợp đồng khách hàng. Dự án sử dụng Google Vision API và các công nghệ hiện đại để đảm bảo hiệu quả và tính tiện lợi.

⚙️ Chức Năng Chính
1. Nhận diện văn bản:
   - Xử lý các định dạng file ảnh như .jpg, .png, .bmp, và PDF.
   - Tiền xử lý ảnh để cải thiện độ chính xác OCR.
   - Tính toán các chỉ số:
     - CER (Character Error Rate).
     - WER (Word Error Rate).

2. Quản lý hợp đồng:
   - Tạo, chỉnh sửa, và xóa hợp đồng.
   - Liên kết hợp đồng với khách hàng.
   - Hiển thị danh sách hợp đồng và thông tin chi tiết.

3. Tích hợp frontend-backend:
   - Giao diện người dùng thân thiện với Vue.js.
   - Backend API sử dụng Flask.

4. Triển khai với Docker:
   - Dễ dàng cài đặt và chạy toàn bộ hệ thống với Docker Compose.
   - Hỗ trợ HTTPS thông qua Nginx reverse proxy.

🛠️ Công Nghệ Sử Dụng
- Frontend:
  - Vue.js
  - HTML, CSS (Scoped Styles)

- Backend:
  - Python Flask
  - Google Vision API

- Triển khai:
  - Docker
  - Nginx

🚀 Hướng Dẫn Cài Đặt

1. Clone Repository
   git clone https://github.com/MinhQuan249/vts-management.git
   cd vts-management

2. Cấu Hình Biến Môi Trường
- Thêm file vision-key.json (Google Vision API key) vào thư mục secrets/.
- Đảm bảo .gitignore đã loại trừ secrets/.

3. Chạy Dự Án Với Docker
- Build và chạy container:
  docker-compose up --build
- Truy cập các dịch vụ:
  - Frontend: http://localhost
  - Backend API: http://localhost:5000/ocr/upload

🖼️ Giao Diện Người Dùng
- Form Upload File OCR:
  - Tải lên file ảnh hoặc PDF.
  - Nhập ground truth để tính CER/WER.

- Quản Lý Hợp Đồng:
  - Hiển thị danh sách hợp đồng.
  - Tạo mới, chỉnh sửa, hoặc xóa hợp đồng.
  - Liên kết với khách hàng.

📊 JSON Trả Về Từ Backend (Ví Dụ)
{
  "results": [
    {
      "image": "uploaded_image.jpg",
      "library": "Google Vision API",
      "text": "Recognized text from the image",
      "confidence": "94.50%",
      "time": "150 ms",
      "cer_accuracy": "98.20%",
      "cer": 0.018,
      "wer_accuracy": "96.30%",
      "wer": 0.037,
      "handwritingSupport": "Tốt",
      "vietnameseSupport": "Có"
    }
  ]
}

🛡️ Các Vấn Đề Đã Giải Quyết
1. Push Protection: Đảm bảo không đẩy các file nhạy cảm lên GitHub bằng .gitignore.
2. HTTPS: Triển khai HTTPS với Nginx reverse proxy.
3. Xử Lý Lỗi: Log chi tiết trên backend và giao diện thông báo lỗi rõ ràng.

🌱 Hướng Phát Triển Trong Tương Lai
- Tích hợp thêm công cụ OCR khác (Tesseract, EasyOCR) để so sánh hiệu quả.
- Cải thiện giao diện người dùng với thông báo chi tiết hơn.
- Tích hợp thêm tính năng báo cáo tổng hợp kết quả OCR và hiệu suất.

Nếu bạn có bất kỳ vấn đề hoặc thắc mắc nào về dự án, hãy liên hệ hoặc tạo issue trên GitHub Repository! 🚀
