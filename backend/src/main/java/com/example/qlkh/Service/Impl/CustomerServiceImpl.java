package com.example.qlkh.Service.Impl;

import com.example.qlkh.Entity.Customer;
import com.example.qlkh.Entity.Employee;
import com.example.qlkh.Repository.CustomerRepository;
import com.example.qlkh.Repository.EmployeeRepository;
import com.example.qlkh.Service.CustomerService;
import com.example.qlkh.dto.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private EmployeeRepository employeeRepository;

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return convertToDTO(customer);
    }

    @Override
    public CustomerDTO addCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setAddress(customerDTO.getAddress());
        customer.setPhone(customerDTO.getPhone());

        if (customerDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(customerDTO.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + customerDTO.getEmployeeId()));
            customer.setEmployee(employee);
        }

        Customer savedCustomer = customerRepository.save(customer);

        // Chuyển đổi từ Customer sang CustomerDTO
        CustomerDTO savedCustomerDTO = new CustomerDTO();
        savedCustomerDTO.setId(savedCustomer.getId());
        savedCustomerDTO.setName(savedCustomer.getName());
        savedCustomerDTO.setAddress(savedCustomer.getAddress());
        savedCustomerDTO.setPhone(savedCustomer.getPhone());
        savedCustomerDTO.setEmployeeId(savedCustomer.getEmployee() != null ? savedCustomer.getEmployee().getId() : null);

        return savedCustomerDTO;
    }




    @Override
    public CustomerDTO updateCustomer(Integer id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setPhone(customerDTO.getPhone());
        existingCustomer.setAddress(customerDTO.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return convertToDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    // Helper methods to convert between DTO and Entity
    private CustomerDTO convertToDTO(Customer customer) {
        return new CustomerDTO(customer.getId(), customer.getName(), customer.getPhone(), customer.getAddress());
    }

    private Customer convertToEntity(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setId(customerDTO.getId());
        customer.setName(customerDTO.getName());
        customer.setPhone(customerDTO.getPhone());
        customer.setAddress(customerDTO.getAddress());
        return customer;
    }
}
