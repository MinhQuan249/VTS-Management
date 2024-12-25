import axios from "axios";
const API_URL = import.meta.env.VITE_API_URL + '/customers';



// Lấy danh sách khách hàng
export const getCustomers = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error fetching customers:", error.response || error.message);
    console.log("VITE_API_URL:", import.meta.env.VITE_API_URL);
    console.log("API URL:", API_URL);
    throw error;
  }
};

// Thêm mới khách hàng
export const addCustomer = async (customer) => {
  try {
    const response = await axios.post(API_URL, customer);
    return response.data;
  } catch (error) {
    console.error("Error adding customer:", error);
    throw error;
  }
};

// Xóa khách hàng
export const deleteCustomer = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error deleting customer with ID: ${id}`, error);
    throw error;
  }
};

// Cập nhật thông tin khách hàng
export const updateCustomer = async (id, data) => {
  try {
    const response = await axios.put(`${API_URL}/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`Error updating customer with ID: ${id}`, error);
    throw error;
  }
};
