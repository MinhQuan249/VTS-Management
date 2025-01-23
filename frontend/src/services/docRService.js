import axios from "axios";

// Base URL cho Document Repository
const API_URL = import.meta.env.VITE_API_URL + '/documents';

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
