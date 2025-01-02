<template>
  <div class="ocr-container">
    <h1 data-aos="fade-up">Nhận diện chữ viết (OCR)</h1>

    <!-- Form tải file -->
    <form @submit.prevent="handleFileUpload">
      <label for="file" class="upload-label">Tải lên file (PDF, hình ảnh):</label>
      <input
          type="file"
          id="file"
          @change="onFileChange"
          accept=".pdf,.png,.jpg,.jpeg"
      />

      <!-- Hiển thị ảnh preview -->
      <div v-if="previewImage || file" class="image-preview-container">
        <h3>Xem trước:</h3>
        <template v-if="previewImage">
          <img
              :src="previewImage"
              :style="{ transform: `rotate(${rotation}deg)` }"
              alt="Preview"
              class="image-preview"
          />
          <div class="rotate-controls">
            <button type="button" @click="setRotation(0)">0°</button>
            <button type="button" @click="setRotation(90)">90°</button>
            <button type="button" @click="setRotation(180)">180°</button>
            <button type="button" @click="setRotation(270)">270°</button>
            <span>Góc hiện tại: {{ rotation }}°</span>
          </div>
        </template>
        <template v-else-if="file?.type === 'application/pdf'">
          <iframe
              :src="pdfViewerUrl"
              width="100%"
              height="500px"
              frameborder="0"
              title="PDF Preview"
          ></iframe>
        </template>
      </div>

      <button type="submit" :disabled="isUploading">Nhận diện</button>
    </form>

    <!-- Hiển thị tiến trình upload -->
    <div v-if="isUploading" class="progress-container">
      <p>Đang tải lên: {{ uploadProgress }}%</p>
      <progress max="100" :value="uploadProgress"></progress>
    </div>

    <!-- Hiển thị kết quả OCR -->
    <div v-if="ocrResults.length">
      <h2>Kết quả nhận diện:</h2>
      <div v-for="(result, index) in ocrResults" :key="index" class="ocr-result">
        <h3>Trang/Ảnh: {{ result.image }}</h3>
        <textarea readonly rows="10" cols="50">{{ result.text }}</textarea>
        <p>Thời gian xử lý: {{ result.time }}</p>
      </div>

      <!-- Nút So Sánh -->
      <button @click="compareText" :disabled="isComparing">So sánh</button>
    </div>

    <!-- Hiển thị kết quả So sánh -->
    <div v-if="comparisonResults.length">
      <h2>Kết quả So sánh:</h2>
      <table>
        <thead>
        <tr>
          <th>Tên File</th>
          <th>Cosine Similarity (%)</th>
          <th>Jaccard Similarity (%)</th>
          <th>Đoạn Văn Bản Trùng Lặp</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="result in comparisonResults" :key="result.document_id">
          <td>{{ result.fileName }}</td>
          <td>{{ (result.cosine_similarity * 100).toFixed(2) }}%</td>
          <td>{{ (result.jaccard_similarity * 100).toFixed(2) }}%</td>
          <td>
            <button @click="showCommonTexts(result.common_texts)">Xem đoạn trùng lặp</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import AOS from "aos";
import "aos/dist/aos.css";
import {getDocuments} from "@/services/docRService.js";

export default {
  data() {
    return {
      file: null,
      previewImage: null,
      ocrResults: [],
      comparisonResults: [],
      rotation: 0,
      isUploading: false,
      isComparing: false,
      uploadProgress: 0,
    };
  },
  computed: {
    pdfViewerUrl() {
      if (this.file && this.file.type === "application/pdf") {
        return URL.createObjectURL(this.file);
      }
      return null;
    },
  },
  mounted() {
    AOS.init();
  },
  methods: {
    setRotation(angle) {
      this.rotation = angle;
      this.updatePreviewImage();
    },
    showCommonTexts(commonTexts) {
      if (commonTexts.length) {
        alert(`Đoạn văn bản trùng lặp:\n${commonTexts.join("\n")}`);
      } else {
        alert("Không có đoạn văn bản trùng lặp nào.");
      }
    },
    async updatePreviewImage() {
      if (this.file) {
        const reader = new FileReader();
        reader.onload = async (e) => {
          const image = new Image();
          image.src = e.target.result;

          image.onload = async () => {
            const rotatedImage = await this.rotateImage(image, this.rotation);
            this.previewImage = rotatedImage.previewUrl;
            this.file = rotatedImage.file;
          };
        };
        reader.readAsDataURL(this.file);
      }
    },
    onFileChange(event) {
      this.file = event.target.files[0];
      const allowedTypes = ["application/pdf", "image/png", "image/jpeg", "image/jpg"];
      if (!allowedTypes.includes(this.file.type)) {
        alert("Vui lòng tải lên file định dạng PDF hoặc hình ảnh.");
        this.file = null;
        return;
      }

      const reader = new FileReader();
      reader.onload = async (e) => {
        const image = new Image();
        image.src = e.target.result;

        image.onload = async () => {
          const rotatedImage = await this.rotateImage(image, this.rotation);
          this.previewImage = rotatedImage.previewUrl;
          this.file = rotatedImage.file;
        };
      };
      reader.readAsDataURL(this.file);
    },
    async rotateImage(image, angle) {
      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d");

      canvas.width = angle % 180 === 0 ? image.width : image.height;
      canvas.height = angle % 180 === 0 ? image.height : image.width;

      ctx.translate(canvas.width / 2, canvas.height / 2);
      ctx.rotate((angle * Math.PI) / 180);
      ctx.drawImage(image, -image.width / 2, -image.height / 2);

      return new Promise((resolve) => {
        canvas.toBlob((blob) => {
          const file = new File([blob], this.file.name, { type: this.file.type });
          resolve({ previewUrl: canvas.toDataURL(), file });
        });
      });
    },
    async handleFileUpload() {
      if (!this.file) {
        alert("Vui lòng chọn một file.");
        return;
      }

      const formData = new FormData();
      formData.append("file", this.file);

      this.isUploading = true;
      this.uploadProgress = 0;

      try {
        const response = await axios.post(
            `${import.meta.env.VITE_API_URL}/ocr/upload`,
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
              },
              onUploadProgress: (progressEvent) => {
                this.uploadProgress = Math.round(
                    (progressEvent.loaded / progressEvent.total) * 100
                );
              },
            }
        );

        this.ocrResults = response.data.results || [];
        console.log("OCR Results:", this.ocrResults);
      } catch (error) {
        console.error("Lỗi khi gửi file:", error);
        alert(error.response?.data || "Có lỗi xảy ra khi nhận diện.");
      } finally {
        this.isUploading = false;
      }
    },
    async compareText() {
      if (!this.ocrResults.length) {
        alert("Không có kết quả nhận diện để so sánh.");
        return;
      }

      this.isComparing = true;

      try {
        // Lấy danh sách tài liệu từ backend
        const documents = await getDocuments();

        // Định dạng danh sách tài liệu
        const formattedDocuments = documents.map((doc) => ({
          id: doc.id,
          text: doc.extractedText,
          fileName: doc.fileName || "Tên không xác định", // Xử lý nếu thiếu fileName
        }));

        // Gửi yêu cầu so sánh
        const response = await axios.post(
            `${import.meta.env.VITE_API_URL}/ocr/compare`,
            {
              text: this.ocrResults.map((result) => result.text).join("\n"), // Văn bản từ OCR
              documents: formattedDocuments.map((doc) => ({
                id: doc.id,
                text: doc.text,
              })), // Chỉ gửi id và text đến backend
            }
        );

        // Gắn tên file vào kết quả
        const results = response.data.results || [];
        this.comparisonResults = results.map((result) => ({
          ...result,
          fileName:
              formattedDocuments.find((doc) => doc.id === result.document_id)
                  ?.fileName || "Tên không xác định", // Ánh xạ fileName theo document_id
        }));

        console.log("Kết quả so sánh:", this.comparisonResults);
      } catch (error) {
        console.error("Lỗi so sánh:", error);
        alert("Không thể thực hiện so sánh.");
      } finally {
        this.isComparing = false;
      }
    },
  },
};
</script>

<style scoped>
body {
  font-family: "Inter", sans-serif;
  background: linear-gradient(to bottom, #fff5e6, #ffe0b2);
}

.ocr-container {
  max-width: 900px;
  margin: auto;
  padding: 20px;
  border-radius: 12px;
  background: url('../components/images/img2.jpeg') no-repeat center center;
  background-size: cover;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h1 {
  font-size: 32px;
  font-weight: bold;
  text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
  color: #2c3e50;
  margin-bottom: 20px;
}

form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

input[type="file"] {
  padding: 10px;
  border: 2px dashed #3498db;
  border-radius: 6px;
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  transition: border-color 0.3s, background-color 0.3s;
  text-align: center;
}

input[type="file"]:hover {
  border-color: #2980b9;
  background-color: #eaf5fd;
}

textarea {
  width: 80%;
  min-height: 100px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ddd;
  font-size: 14px;
  resize: none;
  transition: border-color 0.3s;
}

textarea:focus {
  border-color: #3498db;
  outline: none;
}

button {
  padding: 12px 20px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 6px;
  border: none;
  background-color: #2ecc71;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.3s;
}

button:hover {
  background-color: #27ae60;
  transform: scale(1.05);
}

.image-preview-container {
  margin-top: 20px;
  text-align: center;
}

.image-preview {
  max-width: 100%;
  height: auto;
  margin-bottom: 10px;
  border: 2px solid #ddd;
  border-radius: 8px;
  transition: transform 0.3s;
}

.image-preview:hover {
  transform: scale(1.05);
}

.progress-container {
  margin-top: 20px;
  text-align: center;
  color: #2c3e50;
}

.ocr-summary th {
  background-color: navy;
  color: white;
  padding: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.ocr-summary td {
  text-align: center;
  padding: 10px;
  color: #333;
}

.ocr-summary tr:nth-child(even) {
  background-color: rgba(255, 255, 255, 0.2);
}

.ocr-summary tr:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .ocr-container {
    padding: 15px;
  }

  textarea {
    width: 100%;
  }

  .ocr-summary th,
  .ocr-summary td {
    font-size: 12px;
    padding: 8px;
  }
}
</style>
