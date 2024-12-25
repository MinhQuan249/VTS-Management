package com.example.qlkh.Repository;

import com.example.qlkh.Entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    @Query("SELECT c FROM Contract c JOIN FETCH c.customer WHERE c.id = :id")
    Optional<Contract> findByIdWithCustomer(@Param("id") Integer id);
}
