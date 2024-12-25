<template>
  <div>
    <h1>Employee Management</h1>
    <button @click="openAddEmployeeModal">Add New Employee</button>
    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Position</th>
        <th>Customers</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="employee in employees" :key="employee.id">
        <td>{{ employee.id }}</td>
        <td>{{ employee.name }}</td>
        <td>{{ employee.position }}</td>
        <td>
          <ul>
            <li v-for="customer in employee.customers" :key="customer.id">{{ customer.name }}</li>
          </ul>
        </td>
        <td>
          <button @click="openEditForm(employee)">Edit</button>
          <button @click="deleteEmployee(employee.id)" style="background-color: red;">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>
    <!-- Add Employee Modal -->
    <div v-if="isAddModalOpen" class="modal">
      <h2>Add New Employee</h2>
      <form @submit.prevent="submitNewEmployee">
        <label>Name:</label>
        <input v-model="newEmployee.name" required />
        <label>Position:</label>
        <input v-model="newEmployee.position" required />
        <button type="submit">Save</button>
        <button @click="closeAddEmployeeModal" style="background-color: gold">Cancel</button>
      </form>
    </div>
    <!-- Form chỉnh sửa -->
    <div v-if="selectedEmployee" class="edit-form">
      <h2>Edit Employee</h2>
      <form @submit.prevent="updateEmployeeData">
        <label for="id">ID:</label>
        <input type="text" v-model="selectedEmployee.id" readonly disabled />

        <label for="name">Name:</label>
        <input type="text" v-model="selectedEmployee.name" required />

        <label for="position">Position:</label>
        <input type="text" v-model="selectedEmployee.position" required />

        <label for="customers">Customers:</label>
        <div>
          <ul>
            <li v-for="(customer, index) in selectedEmployee.customers" :key="customer.id">
              {{ customer.name }}
              <button @click.prevent="removeCustomer(index)">Remove</button>
            </li>
          </ul>
          <select v-model="newCustomerId" class = "option">
            <option value="" disabled>Select Customer</option>
            <option
                v-for="customer in availableCustomers"
                :value="customer.id"
                :key="customer.id"
            >
              {{ customer.name }}
            </option>
          </select>
          <button @click.prevent="addCustomer">Add Customer</button>
        </div>

        <button type="submit">Save</button>
        <button type="button" @click="cancelEdit">Cancel</button>
      </form>
    </div>
  </div>
</template>

<script>
import {addEmployee, getEmployees, deleteEmployee, updateEmployee } from "../services/employeeService";
import axios from "axios";
const BASE_API_URL = import.meta.env.VITE_API_URL;
export default {
  data() {
    return {
      employees: [],
      isAddModalOpen: false,
      selectedEmployee: null,
      availableCustomers: [],
      newCustomerId: null,
      newEmployee: {
        name: "",
        position: "",
      },
    };
  },
  methods: {
    async fetchEmployees() {
      try {
        this.employees = await getEmployees();
        // Gọi fetchAvailableCustomers sau khi employees được cập nhật
        this.fetchAvailableCustomers();
      } catch (error) {
        console.error("Error fetching employees:", error);
      }
    },
    openAddEmployeeModal() {
      this.isAddModalOpen = true;
    },
    closeAddEmployeeModal() {
      this.isAddModalOpen = false;
      this.newEmployee = { name: "", position: "" };
    },
    async fetchAvailableCustomers() {
      try {
        const response = await axios.get(`${BASE_API_URL}/customers`);

        // Lấy tất cả ID khách hàng đã được gán
        const assignedCustomerIds = new Set(
            this.employees.flatMap((employee) =>
                employee.customers.map((customer) => customer.id)
            )
        );

        // Lọc ra các khách hàng chưa được gán
        this.availableCustomers = response.data.filter(
            (customer) => !assignedCustomerIds.has(customer.id)
        );
      } catch (error) {
        console.error("Error fetching customers:", error);
      }
    },


    openEditForm(employee) {
      this.selectedEmployee = JSON.parse(JSON.stringify(employee)); // Deep copy
    },
    addCustomer() {
      const customer = this.availableCustomers.find((c) => c.id === this.newCustomerId);
      if (customer) {
        // Kiểm tra khách hàng có trùng không (an toàn hơn)
        const isAlreadyAssigned = this.employees.some((employee) =>
            employee.customers.some((c) => c.id === customer.id)
        );

        if (isAlreadyAssigned) {
          alert("This customer is already assigned to another employee!");
          return;
        }

        this.selectedEmployee.customers.push(customer);
        this.availableCustomers = this.availableCustomers.filter((c) => c.id !== this.newCustomerId);
        this.newCustomerId = null;
        this.fetchAvailableCustomers();
      }
    },
    async submitNewEmployee() {
      try {
        await addEmployee(this.newEmployee);
        this.fetchEmployees(); // Tải lại danh sách nhân viên
        this.fetchAvailableCustomers(); // Làm mới danh sách khách hàng khả dụng
        this.closeAddEmployeeModal();
        alert("Employee added successfully!");
      } catch (error) {
        console.error("Error adding employee:", error);
        if (error.response && error.response.status === 400) {
          alert(error.response.data.message || "Employee already exists.");
        } else {
          alert("Failed to add employee. Please try again.");
        }
      }
    },
    removeCustomer(index) {
      const removedCustomer = this.selectedEmployee.customers[index];
      if (confirm(`Are you sure you want to remove customer "${removedCustomer.name}"?`)) {
        // Gửi yêu cầu cập nhật quan hệ về backend
        axios
            .delete(`${BASE_API_URL}/employees/${this.selectedEmployee.id}/customers/${removedCustomer.id}`)
            .then(() => {
              // Xóa khách hàng khỏi danh sách của nhân viên trên giao diện
              this.selectedEmployee.customers.splice(index, 1);

              // Đưa khách hàng này quay lại danh sách availableCustomers
              this.availableCustomers.push(removedCustomer);

              alert("Removed customer successfully!");
            })
            .catch((error) => {
              console.error("Error removing customer:", error);
              alert("Failed to remove customer. Please try again.");
            });
      }
    },


    async updateEmployeeData() {
      try {
        await updateEmployee(this.selectedEmployee.id, this.selectedEmployee);
        alert("Employee updated successfully!");
        this.selectedEmployee = null;
        this.fetchEmployees();
        this.fetchAvailableCustomers(); // Làm mới danh sách khách hàng khả dụng
      } catch (error) {
        console.error("Error updating employee:", error);
      }
    },
    async deleteEmployee(id) {
      if (confirm("Are you sure you want to delete this employee?")) {
        try {
          await deleteEmployee(id);
          alert("Employee deleted successfully!");
          this.fetchEmployees();
        } catch (error) {
          console.error("Error deleting employee:", error);
        }
      }
    },
    cancelEdit() {
      this.selectedEmployee = null;
      this.fetchAvailableCustomers();
    },
  },
  mounted() {
    this.fetchEmployees();
    this.fetchAvailableCustomers();
  },
};
</script>

<style scoped>
.modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: whitesmoke;
  color: #181818;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal label {
  margin-left: 5px;
}

.modal input {
  margin-left: 5px;
  text-size: 15px;
  border-radius: 8px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1.5rem;
}

h2 {
  color: #3498db;
}

th, td {
  padding: 0.5rem 1rem;
  text-align: left;
  border: 1px solid #ddd;
}

th {
  background-color: #2c3e50;
  color: #ecf0f1;
}

button {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
}

.option {
  color: #181818;
}

.edit-form {
  background-color: bisque;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
  width: 400px;
}

label {
  display: block;
  color: #181818;
  margin: 10px 0 5px;
}

input {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
