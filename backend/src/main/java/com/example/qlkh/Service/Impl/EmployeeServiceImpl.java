package com.example.qlkh.Service.Impl;

import com.example.qlkh.Entity.Customer;
import com.example.qlkh.Entity.Employee;
import com.example.qlkh.Exception.ResourceNotFoundException;
import com.example.qlkh.Repository.CustomerRepository;
import com.example.qlkh.Repository.EmployeeRepository;
import com.example.qlkh.Service.EmployeeService;
import com.example.qlkh.dto.CustomerDTO;
import com.example.qlkh.dto.EmployeeDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private CustomerRepository customerRepository;

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Override
    public Employee addEmployee(EmployeeDTO employeeDTO) {
        // Kiểm tra xem nhân viên đã tồn tại chưa
        boolean exists = employeeRepository.existsByNameAndPosition(employeeDTO.getName(), employeeDTO.getPosition());
        if (exists) {
            throw new IllegalArgumentException("Employee already exists with the same name and position.");
        }

        // Nếu chưa tồn tại, tiếp tục thêm mới
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setPosition(employeeDTO.getPosition());
        return employeeRepository.save(employee);
    }


    @Override
    public void deleteEmployee(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }
    @Override
    public Employee updateEmployee(Integer id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setName(employeeDTO.getName());
        employee.setPosition(employeeDTO.getPosition());

        // Xóa các khách hàng hiện tại
        employee.getCustomers().clear();

        // Thêm khách hàng mới
        for (CustomerDTO customerDTO : employeeDTO.getCustomers()) {
            Customer customer = customerRepository.findById(customerDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            customer.setEmployee(employee);
            employee.getCustomers().add(customer);
        }

        return employeeRepository.save(employee);
    }
    @Transactional
    @Override
    public void removeCustomer(Integer employeeId, Integer customerId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        // Kiểm tra mối quan hệ trước khi xóa
        if (customer.getEmployee() != null && customer.getEmployee().getId().equals(employeeId)) {
            customer.setEmployee(null);
            customerRepository.save(customer);
        } else {
            throw new IllegalStateException("Employee does not manage this customer.");
        }
    }



}
