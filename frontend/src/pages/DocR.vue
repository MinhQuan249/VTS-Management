<template>
  <div class="doc">
    <h1>Document Repository</h1>

    <!-- Main Container -->
    <div class="main-container">
      <!-- Left: Upload Form -->
      <section class="upload-form">
        <form @submit.prevent="handleUpload">
          <!-- File Input -->
          <label for="file">Choose a file:</label>
          <input type="file" id="file" @change="onFileSelect" required />

          <!-- Select Authors -->
          <label for="authors">Select Authors:</label>
          <div v-if="customers.length > 0" class="customer-list">
            <div
                v-for="customer in customers"
                :key="customer.id"
                class="customer-item"
            >
              <input
                  type="checkbox"
                  :id="`customer-${customer.id}`"
                  :value="customer.id"
                  v-model="selectedAuthors"
              />
              <label :for="`customer-${customer.id}`">{{ customer.name }}</label>
            </div>
          </div>

          <p v-if="isLoadingCustomers">Loading customers...</p>
          <p v-else-if="customers.length === 0">No customers available.</p>

          <button type="submit">Upload</button>
        </form>
        <p
            v-if="uploadStatus"
            :class="{ success: isUploadSuccessful, error: !isUploadSuccessful }"
        >
          {{ uploadStatus }}
        </p>
      </section>

      <!-- Right: Document List -->
      <section class="document-panel">
        <h2>Uploaded Documents</h2>

        <!-- Search Bar -->
        <div class="search-bar">
          <input
              type="text"
              placeholder="Search documents..."
              v-model="searchQuery"
              @input="filterDocuments"
          />
          <div class="filters">
            <label for="filter-type">Type:</label>
            <select id="filter-type" v-model="filterType" @change="filterDocuments">
              <option value="">All</option>
              <option value="pdf">PDF</option>
              <option value="docx">Word</option>
              <option value="image">Image</option>
            </select>

            <label for="filter-date">Date:</label>
            <input
                type="date"
                id="filter-date"
                v-model="filterDate"
                @change="filterDocuments"
            />
          </div>
        </div>

        <!-- Document List -->
        <div class="document-list">
          <div
              v-for="doc in filteredDocuments"
              :key="doc.id"
              class="document-item"
          >
            <div class="document-info">
              <h3>{{ doc.fileName }}</h3>
              <p>{{ formatDate(doc.createdAt) }}</p>
            </div>
            <div class="document-actions">
              <button @click="viewDocument(doc.id)">🔍</button>
              <button @click="deleteDocument(doc.id)">🗑️</button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Document Details Modal -->
    <div v-if="isDocumentDetailsVisible" class="document-details">
      <div class="details-container">
        <h3>Document Details</h3>
        <p><strong>Name:</strong> {{ documentDetails.fileName }}</p>
        <p><strong>Created At:</strong> {{ formatDate(documentDetails.createdAt) }}</p>
        <p><strong>Extracted Text:</strong></p>
        <textarea readonly rows="10" cols="50">{{ documentDetails.extractedText }}</textarea>
        <button @click="closeDocumentDetails">Close</button>
      </div>
    </div>
  </div>
</template>

<script>
import {
  uploadDocument,
  getDocuments,
  deleteDocument,
  getDocumentDetails,
} from "../services/docRService";
import { getCustomers } from "../services/customerService";

export default {
  data() {
    return {
      selectedFile: null,
      uploadStatus: "",
      isUploadSuccessful: false,
      documents: [],
      filteredDocuments: [],
      customers: [],
      selectedAuthors: [],
      isLoadingCustomers: true,
      searchQuery: "",
      filterType: "",
      filterDate: "",
      documentDetails: null,
      isDocumentDetailsVisible: false,
    };
  },
  methods: {
    async fetchDocuments() {
      try {
        this.documents = await getDocuments();
        this.filteredDocuments = this.documents;
      } catch (error) {
        console.error("Failed to fetch documents:", error);
      }
    },
    filterDocuments() {
      const query = this.searchQuery.toLowerCase();
      this.filteredDocuments = this.documents.filter((doc) => {
        const matchesQuery = doc.fileName.toLowerCase().includes(query);

        // Xử lý logic lọc theo loại tài liệu
        const matchesType = this.filterType
            ? this.filterType === "image"
                ? /\.(jpg|jpeg|png|gif)$/i.test(doc.fileName) // Kiểm tra các định dạng ảnh
                : doc.fileName.endsWith(this.filterType) // Lọc PDF, Word
            : true;

        const matchesDate = this.filterDate
            ? new Date(doc.createdAt).toISOString().slice(0, 10) === this.filterDate
            : true;

        return matchesQuery && matchesType && matchesDate;
      });
    },
    async deleteDocument(docId) {
      try {
        await deleteDocument(docId);
        this.fetchDocuments();
      } catch (error) {
        console.error("Failed to delete document:", error);
      }
    },
    async viewDocument(docId) {
      try {
        const docDetails = await getDocumentDetails(docId);
        this.documentDetails = docDetails;
        this.isDocumentDetailsVisible = true;
        console.log("Document Details:", docDetails);
      } catch (error) {
        console.error("Failed to fetch document details:", error);
        alert("Failed to fetch document details.");
      }
    },
    closeDocumentDetails() {
      this.isDocumentDetailsVisible = false;
      this.documentDetails = null;
    },
    async fetchCustomers() {
      this.isLoadingCustomers = true;
      try {
        this.customers = await getCustomers();
        console.log("Fetched Customers:", this.customers);
      } catch (error) {
        console.error("Error fetching customers:", error);
      } finally {
        this.isLoadingCustomers = false;
      }
    },
    onFileSelect(event) {
      this.selectedFile = event.target.files[0];
    },
    async handleUpload() {
      if (!this.selectedFile) {
        alert("Please select a file.");
        return;
      }
      try {
        const formData = new FormData();
        formData.append("file", this.selectedFile);
        formData.append("authors", JSON.stringify(this.selectedAuthors));

        const OCR_API_URL = import.meta.env.VITE_API_URL + "/ocr/upload";
        const ocrResponse = await fetch(OCR_API_URL, {
          method: "POST",
          body: formData,
        });
        if (!ocrResponse.ok) {
          const error = await ocrResponse.text();
          console.error("OCR Service Error:", error);
          alert("Failed to extract text from file.");
          return;
        }
        const ocrData = await ocrResponse.json();
        const extractedText = ocrData.results
            .map((result) => result.text)
            .join("\n");
        const response = await uploadDocument(
            this.selectedFile,
            this.selectedAuthors,
            extractedText
        );
        alert("Upload successful!");
        this.fetchDocuments();
      } catch (error) {
        console.error("Failed to upload document:", error);
        alert("Upload failed.");
      }
    },
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },
  },
  mounted() {
    this.fetchCustomers();
    this.fetchDocuments();
  },
};
</script>
<style scoped>
.main-container {
  display: flex;
  gap: 40px;
  background: url('../components/images/img_1.png') no-repeat center center;
  background-size: cover;
  padding: 40px;
  border-radius: 10px;
  max-width: 1400px;
  margin: 0 auto;
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.3);
}

.upload-form {
  flex: 1;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.2);
}

.document-panel {
  flex: 2;
  background: inherit;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.2);
  color: #181818;
}

.document-panel h2 {
  font-size: 28px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 20px;
  text-align: center;
}

.search-bar {
  display: flex;
  gap: 15px;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar input[type="text"],
.search-bar input[type="date"],
.search-bar select {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-bar input[type="text"] {
  max-width: 60%;
}
.document-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* Giới hạn 5 tài liệu trên một hàng */
  gap: 20px; /* Khoảng cách giữa các tài liệu */
  margin-top: 20px;
  overflow-x: auto; /* Thanh cuộn ngang nếu có nhiều tài liệu hơn */
  padding-bottom: 10px; /* Khoảng cách dưới cho thanh cuộn */
}
.document-item {
  display: inline-block;
  flex-shrink: 0;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #ddd;
  border-radius: 8px;
  width: 180px; /* Độ rộng cố định */
  text-align: center;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden; /* Đảm bảo không tràn */
}

.document-item:hover {
  transform: scale(1.05);
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.2);
}

.document-info h3 {
  font-size: 14px; /* Giới hạn kích thước font chữ */
  font-weight: bold;
  color: #181818;
  white-space: nowrap; /* Không cho xuống dòng */
  overflow: hidden; /* Giấu nội dung vượt quá */
  text-overflow: ellipsis; /* Thêm dấu "..." khi quá dài */
}

.document-info p {
  font-size: 12px; /* Điều chỉnh font-size ngày tháng */
  color: #888;
}

.document-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.document-actions button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #007bff;
  transition: color 0.2s ease;
}

.document-actions button:hover {
  color: #0056b3;
}

button {
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #218838;
}

.document-details {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  width: 400px;
  text-align: left;
}

.details-container h3 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.details-container textarea {
  width: 100%;
  resize: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.details-container button {
  display: block;
  margin: 20px auto 0;
  padding: 10px 20px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.details-container button:hover {
  background-color: #0056b3;
}
</style>
