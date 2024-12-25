import axios from "axios";
const API_URL = import.meta.env.VITE_API_URL + '/employees';

// URL gốc của Employee API

export const getEmployees = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data; // Danh sách Employee
  } catch (error) {
    console.error("Lỗi khi lấy danh sách nhân viên:", error);
    throw error;
  }
};
export const addEmployee = async (employee) => {
  try {
    const response = await axios.post(API_URL, employee);
    return response.data;
  } catch (error) {
    console.error("Error adding employee:", error);
    throw error;
  }
};
export const getEmployeeById = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data; // Chi tiết Employee
  } catch (error) {
    console.error("Lỗi khi lấy nhân viên:", error);
    throw error;
  }
};

export const createEmployee = async (employee) => {
  try {
    const response = await axios.post(API_URL, employee);
    return response.data; // Employee mới tạo
  } catch (error) {
    console.error("Lỗi khi tạo nhân viên:", error);
    throw error;
  }
};

export const updateEmployee = async (id, employee) => {
  try {
    const response = await axios.put(`${API_URL}/${id}`, employee);
    return response.data; // Employee sau khi cập nhật
  } catch (error) {
    console.error("Lỗi khi cập nhật nhân viên:", error);
    throw error;
  }
};

export const deleteEmployee = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/${id}`);
    return response.data; // Xác nhận xóa
  } catch (error) {
    console.error("Lỗi khi xóa nhân viên:", error);
    throw error;
  }
};
