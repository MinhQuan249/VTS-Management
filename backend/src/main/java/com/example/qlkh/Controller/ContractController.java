package com.example.qlkh.Controller;

import com.example.qlkh.Entity.Contract;
import com.example.qlkh.Service.ContractService;
import com.example.qlkh.dto.ContractDTO;
import com.example.qlkh.dto.CustomerDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<ContractDTO> getAllContracts() {
        return contractService.getAllContracts(); // Trả về DTO
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> getContractById(@PathVariable Integer id) {
        Contract contract = contractService.getContractById(id);
        ContractDTO contractDTO = new ContractDTO(
                contract.getId(),
                contract.getAmount(),
                contract.getContractDate(),
                contract.getCustomer() != null ? contract.getCustomer().getId() : null,
                contract.getCustomer() != null ? contract.getCustomer().getName() : "N/A"
        );
        return ResponseEntity.ok(contractDTO);
    }

    @PostMapping
    public ResponseEntity<ContractDTO> addContract(@RequestBody ContractDTO contractDTO) {
        ContractDTO savedContract = contractService.addContract(contractDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContract);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContract(@PathVariable Integer id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok("Contract deleted with id: " + id);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ContractDTO> updateContract(
            @PathVariable Integer id,
            @RequestBody Contract updatedContract
    ) {
        // Gọi service và nhận DTO trực tiếp
        ContractDTO updatedContractDTO = contractService.updateContract(id, updatedContract);

        // Trả về DTO
        return ResponseEntity.ok(updatedContractDTO);
    }



}
