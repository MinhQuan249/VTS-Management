FROM python:3.12-slim

WORKDIR /app

# Cài đặt thư viện
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ mã nguồn vào container
COPY . .

# Đặt PYTHONPATH
ENV PYTHONPATH=/app

# Expose port
EXPOSE 5000

# Chạy ứng dụng Flask
CMD ["python", "app/main.py"]
