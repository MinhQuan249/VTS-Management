package com.example.qlkh.Repository;

import com.example.qlkh.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsByNameAndPosition(String name, String position);
}
