import { createRouter, createWebHistory } from "vue-router";
import Customer from "../pages/Customer.vue";
import Employee from "../pages/Employee.vue";
import Contract from "../pages/Contract.vue";
import OCR from "../pages/OCR.vue";

const routes = [
    { path: "/customers", component: Customer },
    { path: "/employees", component: Employee },
    { path: "/contracts", component: Contract },
    { path: "/ocr", component: OCR },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
