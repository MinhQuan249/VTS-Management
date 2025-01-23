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
          accept=".pdf,.png,.jpg,.jpeg,.tiff"
          multiple
      />

      <!-- Hiển thị preview cho file -->
      <div v-if="previewImages.length" class="image-preview-container">
        <h3>Xem trước:</h3>
        <div v-for="(item, index) in previewImages" :key="index" class="image-preview-item">
          <template v-if="item.type === 'image'">
            <img
                :src="item.previewUrl"
                alt="Preview"
                class="image-preview"
            />

            <!-- Controls for Image Adjustments -->
            <div class="adjust-controls">
              <button type="button" @click="toggleAdjustment(index)">Chỉnh sửa ảnh</button>
              <div v-if="item.showAdjustments" class="adjustment-panel">
                <label>
                  Độ sáng:
                  <input
                      type="range"
                      min="-100"
                      max="100"
                      step="1"
                      v-model.number="item.brightness"
                      @input="applyAdjustments(index)"
                  />
                </label>
                <label>
                  Độ tương phản:
                  <input
                      type="range"
                      min="-100"
                      max="100"
                      step="1"
                      v-model.number="item.contrast"
                      @input="applyAdjustments(index)"
                  />
                </label>
                <label>
                  Ngưỡng nhị phân:
                  <input
                      type="range"
                      min="0"
                      max="255"
                      step="1"
                      v-model.number="item.binarizationThreshold"
                      @input="applyAdjustments(index)"
                  />
                </label>
                <label>
                  Bộ lọc màu xám:
                  <input
                      type="checkbox"
                      v-model="item.applyGrayscale"
                      @change="applyAdjustments(index)"
                  />
                </label>
                <label>
                  Lật ảnh:
                  <button type="button" @click="flipImage(index, 'horizontal')">Lật ngang</button>
                  <button type="button" @click="flipImage(index, 'vertical')">Lật dọc</button>
                </label>
                <label>
                  Cắt ảnh:
                  <button type="button" @click="startCrop(index)">Bắt đầu cắt</button>
                  <button type="button" v-if="item.isCropping" @click="applyCrop(index)">Áp dụng cắt</button>
                </label>
                <label>
                  Resize:
                  <input type="number" v-model.number="item.resizeWidth" placeholder="Chiều rộng" />
                  <input type="number" v-model.number="item.resizeHeight" placeholder="Chiều cao" />
                  <button type="button" @click="applyResize(index)">Áp dụng</button>
                </label>
                <label>
                  Làm mờ:
                  <input
                      type="range"
                      min="1"
                      max="15"
                      step="1"
                      v-model.number="item.blurKernel"
                      @input="applyAdjustments(index)"
                  />
                </label>
                <label>
                  Làm sắc nét:
                  <button type="button" @click="applySharpen(index)">Áp dụng</button>
                </label>
                <label>
                  Phát hiện cạnh:
                  <select v-model="item.edgeDetectionType" @change="applyEdgeDetection(index)">
                    <option value="canny">Canny</option>
                    <option value="sobel">Sobel</option>
                  </select>
                </label>
                <label>
                  Chuyển grayscale:
                  <button type="button" @click="applyGrayscale(index)">Áp dụng</button>
                </label>
                <label>
                  Loại bỏ nhiễu:
                  <button type="button" @click="removeNoise(index)">Áp dụng</button>
                </label>
                <label>
                  Cân bằng histogram:
                  <button type="button" @click="equalizeHistogram(index)">Áp dụng</button>
                </label>
                <label>
                  Invert màu:
                  <button type="button" @click="invertColors(index)">Áp dụng</button>
                </label>
              </div>
              <button type="button" @click="applyAllAdjustments(index)">Áp dụng</button>
              <div class="rotate-controls">
                <button type="button" @click="setRotation(index, 0)">0°</button>
                <button type="button" @click="setRotation(index, 90)">90°</button>
                <button type="button" @click="setRotation(index, 180)">180°</button>
                <button type="button" @click="setRotation(index, 270)">270°</button>
                <span>Góc hiện tại: {{ item.rotation }}°</span>
              </div>
            </div>
          </template>
          <template v-else-if="item.type === 'pdf'">
            <iframe
                :src="item.previewUrl"
                width="100%"
                height="500px"
                frameborder="0"
                title="PDF Preview"
            ></iframe>
          </template>
        </div>
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
          <th>Jaccard Similarity (%)</th>
          <th>Đoạn Văn Bản Trùng Lặp</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="result in comparisonResults" :key="result.document_id">
          <td>{{ result.fileName }}</td>
          <td>{{ (result.jaccard_similarity * 100).toFixed(2) }}%</td>
          <td>
            <button @click="showCommonTexts(result)">Xem đoạn trùng lặp</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <!-- Modal hiển thị so sánh -->
    <div v-if="isComparisonModalVisible" class="comparison-modal">
      <h3>So sánh văn bản</h3>
      <div class="comparison-container">
        <div class="text-container">
          <h4>Văn bản OCR</h4>
          <div v-html="comparisonData.ocrText"></div>
        </div>
        <div class="text-container">
          <h4>Văn bản gốc: {{ comparisonData.fileName }}</h4>
          <div v-html="comparisonData.compareText"></div>
        </div>
      </div>
      <button @click="isComparisonModalVisible = false">Đóng</button>
    </div>
  </div>
</template>

  <script>
  import axios from "axios";
  import AOS from "aos";
  import "aos/dist/aos.css";
  import {
    adjustBrightness,
    adjustContrast,
    applyGrayscale,
    applyBinarization,
    flipImageHorizontally,
    flipImageVertically,
    resizeImage,
    applyBlur,
    applySharpen,
    applyEdgeDetection,
    removeNoise,
  } from "@/services/imageService.js";
  import { getDocuments } from "@/services/docRService.js";

function preprocessText(text) {
  return text
      .normalize("NFKC") // Chuẩn hóa Unicode
      .replace(/[^\p{L}\p{N}\s]+/gu, "") // Loại bỏ ký tự đặc biệt, giữ lại ký tự chữ và số (Unicode-aware)
      .replace(/\s+/g, " ") // Loại bỏ khoảng trắng thừa
      .toLowerCase()
      .trim();
}
export default {
  data() {
    return {
      files: [],
      previewImages: [],
      ocrResults: [],
      comparisonResults: [],
      isUploading: false,
      isComparing: false,
      uploadProgress: 0,
      formattedDocuments: [],
      isComparisonModalVisible: false,
      comparisonData: {},
    };
  },
  mounted() {
    AOS.init();
  },
  methods: {
    toggleAdjustment(index) {
      this.previewImages[index].showAdjustments = !this.previewImages[index].showAdjustments;
    },
    async applyAdjustments(index) {
      const item = this.previewImages[index];
      let updatedFile = item.file;

      if (item.brightness !== 0) {
        updatedFile = await adjustBrightness(updatedFile, item.brightness);
      }
      if (item.contrast !== 0) {
        updatedFile = await adjustContrast(updatedFile, item.contrast);
      }
      if (item.applyGrayscale) {
        updatedFile = await applyGrayscale(updatedFile);
      }
      if (item.binarizationThreshold !== null) {
        updatedFile = await applyBinarization(updatedFile, item.binarizationThreshold);
      }
      if (item.blurKernel > 1) {
        updatedFile = await applyBlur(updatedFile, item.blurKernel);
      }
      if (item.edgeDetectionType) {
        updatedFile = await applyEdgeDetection(updatedFile, item.edgeDetectionType);
      }
      if (item.isRemoveNoise) {
        updatedFile = await removeNoise(updatedFile);
      }
      if (item.isSharpen) {
        updatedFile = await applySharpen(updatedFile);
      }
      if (item.resizeWidth && item.resizeHeight) {
        updatedFile = await resizeImage(updatedFile, item.resizeWidth, item.resizeHeight);
      }

      // Update the file and preview
      this.previewImages[index].file = updatedFile;
      this.previewImages[index].previewUrl = URL.createObjectURL(updatedFile);
      console.log(`Image updated: ${this.previewImages[index].previewUrl}`);
    },
    async flipImage(index, direction) {
      const item = this.previewImages[index];
      const updatedFile =
          direction === "horizontal"
              ? await flipImageHorizontally(item.file)
              : await flipImageVertically(item.file);

      this.previewImages[index].file = updatedFile;
      this.previewImages[index].previewUrl = URL.createObjectURL(updatedFile);
    },

    async applyAllAdjustments(index) {
      const item = this.previewImages[index];
      let updatedFile = item.file;

      if (item.brightness !== 0) {
        updatedFile = await adjustBrightness(updatedFile, item.brightness);
      }
      if (item.contrast !== 0) {
        updatedFile = await adjustContrast(updatedFile, item.contrast);
      }
      if (item.applyGrayscale) {
        updatedFile = await applyGrayscale(updatedFile);
      }
      if (item.binarizationThreshold !== null) {
        updatedFile = await applyBinarization(updatedFile, item.binarizationThreshold);
      }
      if (item.blurKernel > 1) {
        updatedFile = await applyBlur(updatedFile, item.blurKernel);
      }
      if (item.edgeDetectionType) {
        updatedFile = await applyEdgeDetection(updatedFile, item.edgeDetectionType);
      }

      // Cập nhật lại file và preview
      this.previewImages[index].file = updatedFile;
      this.previewImages[index].previewUrl = URL.createObjectURL(updatedFile);
      console.log(`Ảnh sau khi chỉnh sửa đã được cập nhật: ${this.previewImages[index].previewUrl}`);
    },
    showComparisonModal(data) {
      this.comparisonData = data;
      this.isComparisonModalVisible = true;
    },
    async setRotation(index, angle) {
      if (this.previewImages[index] && this.previewImages[index].type === "image") {
        try {
          const { previewUrl, file } = await this.rotateImage(this.previewImages[index].file, angle);

          // Cập nhật lại danh sách previewImages
          this.previewImages[index] = {
            ...this.previewImages[index],
            rotation: angle,
            previewUrl, // Cập nhật URL hiển thị ảnh xoay
            file, // Thay thế file gốc bằng file đã xoay
          };
        } catch (error) {
          console.error("Lỗi khi xoay ảnh:", error);
          alert("Không thể xoay ảnh.");
        }
      }
    },

    showCommonTexts(result) {
      // Lấy và chuẩn hóa văn bản OCR
      const ocrText = preprocessText(this.ocrResults.map((ocrResult) => ocrResult.text).join("\n"));

      // Lấy và chuẩn hóa văn bản gốc
      const compareText = preprocessText(
          this.formattedDocuments.find((doc) => doc.id === result.document_id)?.text || ""
      );

      const { common_texts: commonTexts } = result;

      console.log("Processed OCR Text:", ocrText);
      console.log("Processed Compare Text:", compareText);
      console.log("Common Texts:", commonTexts);

      // Kiểm tra dữ liệu
      if (!ocrText || !compareText || !commonTexts || commonTexts.length === 0) {
        alert("Không có đoạn văn bản trùng lặp nào để hiển thị.");
        return;
      }

      // Highlight các đoạn trùng lặp
      const highlightedOCRText = this.highlightCommonTexts(ocrText, commonTexts);
      const highlightedCompareText = this.highlightCommonTexts(compareText, commonTexts);

      this.showComparisonModal({
        ocrText: highlightedOCRText,
        compareText: highlightedCompareText,
        fileName: result.fileName,
      });
    },

    highlightCommonTexts(text, commonTexts) {
      if (!text || !commonTexts || commonTexts.length === 0) {
        return text;
      }

      const sortedCommonTexts = [...commonTexts.filter(Boolean)].sort((a, b) => b.length - a.length);

      // Tạo regex từ các đoạn trùng lặp
      const regex = new RegExp(
          sortedCommonTexts.map((re) => re.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|"),
          "gi"
      );
      console.log("Generated Regex:", regex);
      // Thay thế các đoạn trùng lặp bằng HTML
      return text.replace(regex, (match) => `<span class="highlightct">${match}</span>`);
    },

    onFileChange(event) {
      if (!event.target.files || event.target.files.length === 0) {
        alert("Không có file nào được chọn.");
        return;
      }

      const selectedFiles = Array.from(event.target.files);
      const allowedTypes = ["application/pdf", "image/png", "image/jpeg", "image/jpg"];

      // Kiểm tra số lượng file
      if (selectedFiles.length > 10) {
        alert("Bạn chỉ có thể tải lên tối đa 10 file.");
        return;
      }

      this.files = []; // Đảm bảo reset trước khi lưu file
      this.previewImages = []; // Reset danh sách preview

      selectedFiles.forEach((file) => {
        if (!allowedTypes.includes(file.type)) {
          alert(`File ${file.name} không được hỗ trợ.`);
        } else {
          this.files.push(file);
          const reader = new FileReader();
          reader.onload = (e) => {
            if (file.type === "application/pdf") {
              this.previewImages.push({
                file,
                previewUrl: e.target.result,
                type: "pdf",
              });
            } else {
              this.previewImages.push({
                file,
                previewUrl: e.target.result,
                type: "image",
                rotation: 0, // Góc xoay mặc định
              });
            }
          };
          reader.readAsDataURL(file);
        }
      });
    },

    async rotateImage(file, angle) {
      const image = new Image();
      image.src = URL.createObjectURL(file);

      return new Promise((resolve, reject) => {
        image.onload = () => {
          const canvas = document.createElement("canvas");
          const ctx = canvas.getContext("2d");

          canvas.width = angle % 180 === 0 ? image.width : image.height;
          canvas.height = angle % 180 === 0 ? image.height : image.width;

          ctx.translate(canvas.width / 2, canvas.height / 2);
          ctx.rotate((angle * Math.PI) / 180);
          ctx.drawImage(image, -image.width / 2, -image.height / 2);

          canvas.toBlob((blob) => {
            if (!blob) {
              reject("Lỗi khi xoay ảnh.");
              return;
            }
            const rotatedFile = new File([blob], file.name, { type: file.type });
            resolve({ previewUrl: canvas.toDataURL(), file: rotatedFile });
          });
        };
        image.onerror = () => reject("Lỗi khi tải ảnh.");
      });
    },
    async handleFileUpload() {
      if (!this.previewImages || this.previewImages.length === 0) {
        alert("Vui lòng chọn ít nhất một file.");
        return;
      }

      const formData = new FormData();

      // Lấy tất cả file từ danh sách previewImages
      this.previewImages.forEach((item, index) => {
        console.log(`Đang gửi file: ${item.file.name}, Preview URL: ${item.previewUrl}`);
        formData.append("files", item.file);
        URL.revokeObjectURL(item.previewUrl);
      });

      this.isUploading = true;
      this.uploadProgress = 0;

      try {
        const response = await axios.post(
            `${import.meta.env.VITE_API_URL}/ocr/upload`,
            formData,
            {
              headers: { "Content-Type": "multipart/form-data" },
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
        const documents = await getDocuments();
        if (!Array.isArray(documents) || documents.length === 0) {
          throw new Error("Danh sách tài liệu từ backend không hợp lệ.");
        }

        const formattedDocuments = documents.map((doc) => ({
          id: doc.id,
          text: doc.extractedText,
          fileName: doc.fileName || "Tên không xác định",
        }));
        this.formattedDocuments = formattedDocuments;
        const documentMap = new Map(
            formattedDocuments.map((doc) => [doc.id, doc.fileName])
        );

        const response = await axios.post(
            `${import.meta.env.VITE_API_URL}/ocr/compare`,
            {
              text: this.ocrResults.map((result) => result.text).join("\n"),
              documents: formattedDocuments.map(({ id, text }) => ({ id, text })),
            }
        );

        this.comparisonResults = response.data.results.map((result) => ({
          ...result,
          fileName: documentMap.get(result.document_id) || "Tên không xác định",
        }));

        console.log("Kết quả so sánh:", this.comparisonResults);
      } catch (error) {
        console.error("Lỗi so sánh:", error);
        alert(error.message || "Không thể thực hiện so sánh.");
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
.ocr-result {
  width: 800px;
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
  transform: scale(1.1);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease-in-out;
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
.comparison-container {
  display: flex;
  text-align: left;
  gap: 20px;
  padding: 20px;
  font-family: Arial, sans-serif;
  background-color: white; /* Nền trắng */
  color: black; /* Văn bản màu đen */
}

.text-container {
  flex: 1;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
  background-color: #f9f9f9;
  overflow-y: auto;
  max-height: 80vh;
}
::v-deep .highlightct {
  background-color: yellow;
  font-weight: bold;
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
.adjust-controls {
  margin-top: 15px;
  background: #f8f9fa;
  padding: 10px 15px;
  border-radius: 8px;
  border: 1px solid #ddd;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: left;
}

.adjustment-panel label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: bold;
}

.adjustment-panel input[type="range"] {
  width: 70%;
  margin-left: 10px;
}

.adjustment-panel button {
  margin-top: 5px;
  padding: 8px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.adjustment-panel button:hover {
  background-color: #0056b3;
}

.adjustment-panel input[type="checkbox"] {
  margin-right: 10px;
}

.adjustment-panel input[type="range"]:focus {
  outline: none;
  border-color: #3498db;
}

/* Nút thả để hiển thị/ẩn chỉnh sửa ảnh */
.adjust-controls > button {
  display: block;
  margin: 10px auto;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  background-color: #ff9800;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.adjust-controls > button:hover {
  background-color: #e68900;
}
</style>