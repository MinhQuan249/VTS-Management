import axios from "axios";

// Base URL cho Document Repository
const API_URL = import.meta.env.VITE_API_URL + '/documents';

// Upload tài liệu
export const uploadDocument = async (file, authorIds, extractedText = "") => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("authors", JSON.stringify(authorIds)); // Gửi danh sách authorIds
    formData.append("extractedText", extractedText);

    try {
        const response = await axios.post(`${API_URL}/upload`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });
        return response.data;
    } catch (error) {
        console.error("Error uploading document:", error);
        throw error;
    }
};

// Lấy danh sách tài liệu
export const getDocuments = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error("Error fetching documents:", error);
        throw error;
    }
};

// Lấy chi tiết tài liệu
export const getDocumentDetails = async (docId) => {
    try {
        const response = await axios.get(`${API_URL}/${docId}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching document details for ID: ${docId}`, error);
        throw error;
    }
};
// Xóa tài liệu
export const deleteDocument = async (docId) => {
    try {
        const response = await axios.delete(`${API_URL}/${docId}`);
        return response.data;
    } catch (error) {
        console.error("Error deleting document:", error);
        throw error;
    }
};
