FROM python:3.9-slim

# Tạo thư mục làm việc
WORKDIR /app
RUN mkdir -p /app

# Cài đặt torch trước để tránh cài đặt lại
RUN pip install --no-cache-dir torch==2.5.0 --index-url https://download.pytorch.org/whl/cpu
RUN pip install pytesseract --upgrade

# Cài đặt các tiện ích hệ thống cần thiết
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    poppler-utils \
    antiword \
    pkg-config \
    libleptonica-dev \
    libtiff-dev \
    libarchive-dev \
    libcurl4-gnutls-dev \
    libicu-dev \
    libpango1.0-dev \
    libcairo2-dev \
    libfontconfig1-dev \
    libopenjp2-7-dev \
    libjpeg-dev && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Sao chép thư mục mã nguồn của Tesseract (bao gồm cả tessdata)
COPY ./tesseract-5.5.0 /opt/tesseract-source
COPY ./tesseract-5.5.0/tessdata /usr/local/share/tessdata

# Build và cài đặt Tesseract từ mã nguồn
RUN mkdir -p /opt/tesseract-build && \
    cd /opt/tesseract-build && \
    cmake ../tesseract-source \
    -DLeptonica_DIR=/usr/lib/x86_64-linux-gnu/cmake/Leptonica && \
    make && \
    make install && \
    ldconfig


# Sao chép các tệp yêu cầu và cài đặt Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Sao chép mã nguồn ứng dụng
COPY . .

# Thiết lập biến môi trường (sử dụng thư mục tessdata trong /usr/local/share)
ENV TESSDATA_PREFIX=/usr/local/share/tessdata
ENV PYTHONPATH=/app

# Mở cổng 5000 để Flask sử dụng
EXPOSE 5000

# Chạy ứng dụng Flask
CMD ["python", "app/main.py"]
