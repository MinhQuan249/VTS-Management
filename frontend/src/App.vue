<template>
  <div>
    <!-- Thanh điều hướng cố định -->
    <nav class="navbar">
      <router-link to="/customers" class="nav-link">Customer</router-link>
      <router-link to="/employees" class="nav-link">Employee</router-link>
      <router-link to="/contracts" class="nav-link">Contract</router-link>
      <router-link to="/ocr" class="nav-link">OCR</router-link>
    </nav>
    <!-- Nội dung thay đổi phía dưới -->
    <div class="content">
      <router-view></router-view>
    </div>
  </div>
</template>

<style>
/* Định dạng thanh navbar */
.navbar {
  position: fixed; /* Cố định vị trí trên cùng */
  top: 0;
  left: 0;
  width: 100%;
  background-color: #2c3e50; /* Màu nền */
  color: white;
  padding: 10px 20px;
  display: flex;
  gap: 20px;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 1000;
}

/* Định dạng liên kết trong navbar */
.navbar .nav-link {
  color: white;
  text-decoration: none;
  font-weight: bold;
}

.navbar .nav-link:hover {
  text-decoration: underline;
}

/* Phần nội dung bên dưới */
.content {
  margin-top: 60px; /* Đẩy nội dung xuống dưới thanh navbar */
  padding: 20px;
}
body {
  background-color: antiquewhite; /* Màu nền mới, ví dụ màu xám nhạt */
  color: #181818; /* Màu chữ (đảm bảo dễ đọc trên nền mới) */
  margin: 0;
  font-family: Arial, sans-serif; /* Font chữ tổng quát */
}

/* Đảm bảo phần `content` nổi bật */
.content {
  background-color: #f2f2f2; /* Nền trắng cho nội dung */
  border-radius: 8px; /* Bo góc nhẹ cho nội dung */
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1); /* Hiệu ứng bóng mờ */
  padding: 20px;
}

</style>
