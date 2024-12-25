package com.example.qlkh.dto;

import java.time.LocalDate;

public class ContractDTO {
    private Integer id;
    private Integer amount;
    private LocalDate contractDate;
    private Integer customerId;
    private String customerName; // Tên khách hàng

    public ContractDTO(Integer id, Integer amount, LocalDate contractDate, Integer customerId, String customerName) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.contractDate = contractDate;
        this.customerName = customerName;
    }

    // Getters và Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
