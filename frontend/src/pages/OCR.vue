<template>
  <div class="ocr-container">
    <h1>Nhận diện chữ viết (OCR)</h1>

    <!-- Form tải file -->
    <form @submit.prevent="handleFileUpload">
      <label for="file">Tải lên file (PDF, Word, hình ảnh):</label>
      <input
          type="file"
          id="file"
          @change="onFileChange"
          accept=".pdf,.doc,.docx,.png,.jpg,.jpeg,.txt,.doc,.docx"
      />

      <!-- Ô nhập ground truth -->
      <div>
        <label for="groundTruth">Nhập ground truth:</label>
        <textarea id="groundTruth" v-model="groundTruth" rows="4"></textarea>
      </div>

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
        <h3>{{ result.library }}</h3>
        <textarea readonly rows="10" cols="50">{{ result.text }}</textarea>
      </div>
    </div>

    <h2>Bảng tổng hợp kết quả:</h2>
    <div class="ocr-summary-container">
      <table class="ocr-summary">
        <thead>
        <tr>
          <th>Page/Image</th>
          <th>Thư viện</th>
          <th>Độ chính xác CER</th>
          <th>Độ chính xác WER</th>
          <th>Chữ viết tay</th>
          <th>Hỗ trợ Tiếng Việt</th>
          <th>Thời gian xử lý</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(result, index) in ocrResults" :key="index">
          <td>{{ result.page }}</td>
          <td>{{ result.library }}</td>
          <td>{{ result.cerAccuracy }}</td>
          <td>{{ result.werAccuracy }}</td>
          <td>{{ result.handwritingSupport }}</td>
          <td>{{ result.vietnameseSupport }}</td>
          <td>{{ result.time }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      file: null,
      previewImage: null,
      groundTruth: "",
      ocrResults: [],
      rotation: 0,
      isUploading: false,
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
  methods: {
    setRotation(angle) {
      this.rotation = angle;
      this.updatePreviewImage();
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
      const allowedTypes = ["application/pdf", "image/png", "image/jpeg"];
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
      formData.append("ground_truth", this.groundTruth);

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

        const rawResults = response.data.results || [];
        this.ocrResults = rawResults.map((result) => ({
          library: result.library,
          text: result.text,
          cerAccuracy: result.cer_accuracy || "N/A",
          cer: result.cer || "N/A",
          werAccuracy: result.wer_accuracy || "N/A",
          wer: result.wer || "N/A",
          handwritingSupport: result.handwritingSupport || "N/A",
          vietnameseSupport: result.vietnameseSupport || "N/A",
          time: result.time || "N/A",
          page: result.page || result.image || "N/A",
        }));

        console.log("OCR Results Processed:", this.ocrResults);
      } catch (error) {
        console.error("Lỗi khi gửi file:", error);
        alert(error.response?.data || "Có lỗi xảy ra khi nhận diện.");
      } finally {
        this.isUploading = false;
      }
    },
  },
};
</script>

<style scoped>
.rotate-controls button {
  margin: 5px;
  padding: 10px 15px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.rotate-controls button:hover {
  background-color: #2980b9;
}

.rotate-controls span {
  margin-left: 10px;
  font-size: 16px;
}

.ocr-container {
  max-width: 800px;
  margin: auto;
  padding: 20px;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

input[type="file"] {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  transition: border-color 0.3s;
}

input[type="file"]:hover,
input[type="file"]:focus {
  border-color: #3498db;
  outline: none;
}

button {
  padding: 12px;
  background-color: #2ecc71;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #27ae60;
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
  transform: scale(1.02);
}

.progress-container {
  margin-top: 10px;
  text-align: center;
}

textarea {
  width: 100%;
  margin-top: 10px;
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

.ocr-result {
  margin-top: 20px;
  border-top: 1px solid #ccc;
  padding-top: 10px;
}

.ocr-summary-container {
  overflow-x: auto;
}

.ocr-summary {
  width: 100%;
  margin-top: 20px;
  border-collapse: collapse;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.1);
}

.ocr-summary th {
  background-color: #2c3e50;
  color: white;
  font-weight: bold;
  padding: 12px;
  text-transform: uppercase;
  font-size: 14px;
}

.ocr-summary td {
  text-align: center;
  padding: 10px;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  color: #333;
  font-size: 14px;
}

.ocr-summary tr:nth-child(even) td {
  background-color: #f2f2f2;
}

.ocr-summary tr:hover td {
  background-color: #eafaf1;
}

@media (max-width: 768px) {
  .ocr-container {
    max-width: 100%;
    padding: 15px;
  }

  button {
    font-size: 14px;
    padding: 10px;
  }

  textarea {
    font-size: 12px;
  }

  .ocr-summary th,
  .ocr-summary td {
    padding: 8px;
    font-size: 12px;
  }
}
</style>
