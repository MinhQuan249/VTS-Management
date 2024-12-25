package com.example.qlkh.dto;

import java.util.List;

public class EmployeeDTO {
    private Integer id;
    private String name;
    private String position;
    private List<CustomerDTO> customers;

    // Constructor, Getters, Setters

    public EmployeeDTO(Integer id, String name, String position, List<CustomerDTO> customers) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.customers = customers;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public List<CustomerDTO> getCustomers() {
        return customers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setCustomers(List<CustomerDTO> customers) {
        this.customers = customers;
    }
}
