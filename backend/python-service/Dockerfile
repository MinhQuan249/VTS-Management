FROM python:3.10-slim

# Tạo thư mục làm việc
WORKDIR /app
RUN mkdir -p /app

# Cài đặt torch trước để tránh cài đặt lại
RUN pip install --no-cache-dir torch==2.5.0 --index-url https://download.pytorch.org/whl/cpu

# Cài đặt các thư viện từ requirements.txt
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Cài đặt các tiện ích hệ thống cần thiết
RUN apt-get update && apt-get install -y --no-install-recommends \
    poppler-utils \
    antiword && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Sao chép toàn bộ mã nguồn vào container
COPY . .

# Đặt biến môi trường PYTHONPATH
ENV PYTHONPATH=/app

# Mở cổng 5000 để Flask sử dụng
EXPOSE 5000

# Chạy ứng dụng Flask
CMD ["python", "app/main.py"]
