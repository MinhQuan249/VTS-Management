import axios from "axios";
const API_URL = import.meta.env.VITE_API_URL + '/contracts';


export const getContracts = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error("Error fetching contracts:", error);
        throw error;
    }
};
export const addContract = async (contract) => {
    try {
        const response = await axios.post(API_URL, contract);
        return response.data; // Trả về contractDTO từ backend
    } catch (error) {
        console.error("Error adding contract:", error);
        throw error;
    }
};



export const deleteContract = async (id) => {
    try {
        const response = await axios.delete(`${API_URL}/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error deleting contract with ID: ${id}`, error);
        throw error;
    }
};
export const updateContract = async (id, data) => {
    try {
        const response = await axios.put(`${API_URL}/${id}`, data);
        return response.data;
    } catch (error) {
        console.error(`Error updating contract with ID: ${id}`, error);
        throw error;
    }
};
