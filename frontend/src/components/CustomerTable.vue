<template>
  <div>
    <h1>Customer Management</h1>
    <button @click="openAddCustomerModal">Add New Customer</button>
    <!-- Customer Table -->
    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Address</th>
        <th>Phone</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="customer in customers" :key="customer.id">
        <td>{{ customer.id }}</td>
        <td>{{ customer.name }}</td>
        <td>{{ customer.address }}</td>
        <td>{{ customer.phone }}</td>
        <td>
          <button @click="openEditCustomerModal(customer)">Edit</button>
          <button @click="deleteCustomer(customer.id)" style="background-color: #e74c3c">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Add Customer Modal -->
    <div v-if="isAddModalOpen" class="modal">
      <h2>Add New Customer</h2>
      <form @submit.prevent="submitNewCustomer">
        <label>Name:</label>
        <input v-model="newCustomer.name" required />
        <label>Address:</label>
        <input v-model="newCustomer.address" required />
        <label>Phone:</label>
        <input v-model="newCustomer.phone" required />
        <button type="submit">Save</button>
        <button @click="closeAddCustomerModal" style="background-color: gold">Cancel</button>
      </form>
    </div>

    <!-- Edit Customer Modal -->
    <div v-if="isEditModalOpen" class="modal">
      <h2>Edit Customer</h2>
      <form @submit.prevent="submitEditCustomer">
        <label>Name:</label>
        <input v-model="editCustomerData.name" required />
        <label>Address:</label>
        <input v-model="editCustomerData.address" required />
        <label>Phone:</label>
        <input v-model="editCustomerData.phone" required />
        <button type="submit">Save</button>
        <button @click="closeEditCustomerModal" style="background-color: gold">Cancel</button>
      </form>
    </div>
  </div>
</template>

<script>
import { getCustomers, addCustomer, deleteCustomer, updateCustomer } from "@/services/customerService";

export default {
  data() {
    return {
      customers: [],
      isAddModalOpen: false,
      isEditModalOpen: false,
      newCustomer: {
        name: "",
        address: "",
        phone: "",
      },
      editCustomerData: {
        id: "",
        name: "",
        address: "",
        phone: "",
      },
    };
  },
  methods: {
    async fetchCustomers() {
      try {
        this.customers = await getCustomers(); // Lấy danh sách khách hàng từ API
      } catch (error) {
        alert("Error fetching customers!");
        console.error("Error fetching customers:", error);
      }
    },

    async submitNewCustomer() {
      this.fetchCustomers();
      try {
        const duplicateCustomer = this.customers.find(
            (customer) =>
                customer.phone === this.newCustomer.phone
        );

        if (duplicateCustomer) {
          alert("Customer already exists! Please check the name, address, or phone.");
          return;
        }

        // Nếu không trùng, thêm khách hàng mới
        await addCustomer(this.newCustomer);
        this.fetchCustomers(); // Reload danh sách khách hàng
        alert("Customer added successfully!");
        this.closeAddCustomerModal();
      } catch (error) {
        alert("Error adding customer!");
        console.error("Error adding customer:", error);
      }
    },

    async deleteCustomer(id) {
      if (confirm("Are you sure you want to delete this customer?")) {
        try {
          await deleteCustomer(id);
          this.fetchCustomers(); // Reload customer list
          alert("Customer deleted successfully!");
        } catch (error) {
          alert(`Error deleting customer with ID: ${id}`);
          console.error(`Error deleting customer with ID: ${id}`, error);
        }
      }
    },
    async submitEditCustomer() {
      try {
        await updateCustomer(this.editCustomerData.id, this.editCustomerData);
        this.fetchCustomers(); // Reload customer list
        alert("Customer updated successfully!");
        this.closeEditCustomerModal();
      } catch (error) {
        alert(`Error updating customer with ID: ${this.editCustomerData.id}`);
        console.error(`Error updating customer with ID: ${this.editCustomerData.id}`, error);
      }
    },
    openAddCustomerModal() {
      this.isAddModalOpen = true;
    },
    closeAddCustomerModal() {
      this.isAddModalOpen = false;
      this.newCustomer = {name: "", address: "", phone: ""};
    },
    openEditCustomerModal(customer) {
      this.isEditModalOpen = true;
      this.editCustomerData = {...customer};
    },
    closeEditCustomerModal() {
      this.isEditModalOpen = false;
      this.editCustomerData = {id: "", name: "", address: "", phone: ""};
    },
  },
  mounted() {
    this.fetchCustomers();
  },
};
</script>

<style>
/* Modal styling */
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

button {
  margin: 5px;
}
</style>
