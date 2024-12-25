package com.example.qlkh.Service;

import com.example.qlkh.Entity.Employee;
import com.example.qlkh.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    Employee getEmployeeById(Integer id);

    Employee addEmployee(EmployeeDTO employeeDTO);
    void removeCustomer(Integer employeeId, Integer customerId);


    void deleteEmployee(Integer id);
    Employee updateEmployee(Integer id, EmployeeDTO employeeDTO);

}
