<template>
  <div>
    <h1>Contract Management</h1>
    <button @click="openAddContractModal">Add New Contract</button>


    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>Customer Name</th>
        <th>Amount</th>
        <th>Contract Date</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="contract in contracts" :key="contract.id">
        <td>{{ contract.id }}</td>
        <td>{{ contract.customerName || "N/A" }}</td>
        <td>{{ contract.amount }}</td>
        <td>{{ contract.contractDate }}</td>
        <td>
          <button @click="openEditForm(contract)">Edit</button>
          <button @click="deleteContract(contract.id)" style="background-color: red;">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>
    <!-- Add Contract Modal -->
    <div v-if="isAddModalOpen" class="modal">
      <h2>Add New Contract</h2>
      <form @submit.prevent="submitNewContract">
        <label>Amount:</label>
        <input v-model="newContract.amount" type="number" placeholder="Enter amount" required />

        <label>Contract Date:</label>
        <input v-model="newContract.contractDate" type="date" required />

        <label>Customer:</label>
        <select v-model="newContract.kh_id" required>
          <option value="" disabled>Select a customer</option>
          <option v-for="customer in customers" :key="customer.id" :value="customer.id">
            {{ customer.name }}
          </option>
        </select>

        <button type="submit">Save</button>
        <button @click="closeAddContractModal" style="background-color: gold">Cancel</button>
      </form>
    </div>

    <!-- Form chỉnh sửa -->
    <div v-if="selectedContract" class="edit-form">
      <h2>Edit Contract</h2>
      <form @submit.prevent="updateContractData">
        <label for="id">ID:</label>
        <input type="text" v-model="selectedContract.id" readonly disabled />

        <label for="amount">Amount:</label>
        <input type="number" v-model="selectedContract.amount" required />

        <label for="contractDate">Contract Date:</label>
        <input type="date" v-model="selectedContract.contractDate" required />

        <label for="customer">Customer:</label>
        <select v-model="selectedContract.customerId" required>
          <option v-for="customer in customers" :value="customer.id" :key="customer.id">
            {{ customer.name }}
          </option>
        </select>

        <button type="submit">Save</button>
        <button type="button" @click="cancelEdit">Cancel</button>
      </form>
    </div>
  </div>
</template>

<script>
import {
  addContract,
  getContracts,
  deleteContract,
  updateContract,
} from "../services/contractService";
import { getCustomers } from "../services/customerService";

export default {
  data() {
    return {
      isAddModalOpen: false,
      contracts: [],
      customers: [], // Danh sách khách hàng
      selectedContract: null,
      newContract: {
        amount: "",
        contractDate: "",
        kh_id: null, // Liên kết với khách hàng
      },
    };
  },
  methods: {
    async fetchContracts() {
      try {
        this.contracts = await getContracts();
      } catch (error) {
        console.error("Error fetching contracts:", error);
      }
    },
    async fetchCustomers() {
      try {
        this.customers = await getCustomers(); // Sử dụng service để lấy danh sách khách hàng
      } catch (error) {
        console.error("Error fetching customers:", error);
      }
    },
    openAddContractModal() {
      this.isAddModalOpen = true;
    },
    closeAddContractModal() {
      this.isAddModalOpen = false;
      this.newContract = {amount: "", contractDate: "", kh_id: null};
    },
    openEditForm(contract) {
      this.selectedContract = {
        id: contract.id,
        amount: contract.amount,
        contractDate: contract.contractDate,
        customerId: contract.customerId,
      };
    },
    async submitNewContract() {
      try {
        if (
            !this.newContract.amount ||
            !this.newContract.contractDate ||
            !this.newContract.kh_id
        ) {
          alert("Please fill all the required fields!");
          return;
        }

        // Chuyển đổi `kh_id` thành `customerId` để gửi đến Backend
        const contractPayload = {
          amount: this.newContract.amount,
          contractDate: this.newContract.contractDate,
          customerId: this.newContract.kh_id, // Chuyển đổi từ kh_id sang customerId
        };

        const addedContract = await addContract(contractPayload);
        this.contracts.push(addedContract); // Thêm trực tiếp vào danh sách
        alert("Contract added successfully!");
        this.closeAddContractModal();
      } catch (error) {
        alert("Failed to add contract. Please try again.");
        console.error("Error adding contract:", error);
      }
    },
    async updateContractData() {
      try {
        const updatedContract = {
          amount: this.selectedContract.amount,
          contractDate: this.selectedContract.contractDate,
          customerId: this.selectedContract.customerId, // Đúng giá trị
        };
        await updateContract(this.selectedContract.id, updatedContract);
        alert("Contract updated successfully!");
        this.selectedContract = null;
        this.fetchContracts();
      } catch (error) {
        console.error("Error updating contract:", error);
      }
    },
    async deleteContract(id) {
      if (confirm("Are you sure you want to delete this contract?")) {
        try {
          await deleteContract(id);
          alert("Contract deleted successfully!");
          this.fetchContracts();
        } catch (error) {
          console.error("Error deleting contract:", error);
        }
      }
    },
    cancelEdit() {
      this.selectedContract = null;
    },
  },
  mounted() {
    this.fetchContracts();
    this.fetchCustomers();
  },
};
</script>


<style scoped>
/* CSS giữ nguyên */
table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1.5rem;
  background-color: var(--color-background-soft);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2 {
  color: #e74c3c;
}

th,
td {
  padding: 0.75rem 1rem;
  text-align: left;
  border: 1px solid var(--color-border);
}

th {
  background-color: var(--vt-c-indigo);
  color: var(--color-heading);
  font-weight: bold;
  text-transform: uppercase;
}

tr:nth-child(even) {
  background-color: var(--color-background-mute);
}

tr:hover {
  background-color: hsla(160, 100%, 37%, 0.2);
}

td {
  color: var(--color-text);
  font-size: 0.95rem;
}

button {
  padding: 0.5rem 1rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: var(--color-heading);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: hsla(160, 100%, 47%, 1);
}
</style>
