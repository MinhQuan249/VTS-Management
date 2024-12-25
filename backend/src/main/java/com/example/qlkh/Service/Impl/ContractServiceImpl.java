package com.example.qlkh.Service.Impl;

import com.example.qlkh.Entity.Contract;
import com.example.qlkh.Entity.Customer;
import com.example.qlkh.Repository.ContractRepository;
import com.example.qlkh.Repository.CustomerRepository;
import com.example.qlkh.Service.ContractService;
import com.example.qlkh.dto.ContractDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {
    @Autowired
    private CustomerRepository customerRepository;

    private final ContractRepository contractRepository;

    public ContractServiceImpl(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public List<ContractDTO> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(contract -> new ContractDTO(
                        contract.getId(),
                        contract.getAmount(),
                        contract.getContractDate(),
                        contract.getCustomer() != null ? contract.getCustomer().getId() : null, // kh_id
                        contract.getCustomer() != null ? contract.getCustomer().getName() : "N/A" // customerName
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Contract getContractById(Integer id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }

    @Override
    public ContractDTO addContract(ContractDTO contractDTO) {
        Contract contract = new Contract();
        contract.setAmount(contractDTO.getAmount());
        contract.setContractDate(contractDTO.getContractDate());

        // Kiểm tra và gán khách hàng nếu customerId được cung cấp
        if (contractDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(contractDTO.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + contractDTO.getCustomerId()));
            contract.setCustomer(customer);
        }

        Contract savedContract = contractRepository.save(contract);

        return new ContractDTO(
                savedContract.getId(),
                savedContract.getAmount(),
                savedContract.getContractDate(),
                savedContract.getCustomer() != null ? savedContract.getCustomer().getId() : null,
                savedContract.getCustomer() != null ? savedContract.getCustomer().getName() : "N/A"
        );
    }

    @Override
    public void deleteContract(Integer id) {
        if (!contractRepository.existsById(id)) {
            throw new RuntimeException("Contract not found with id: " + id);
        }
        contractRepository.deleteById(id);
    }
    @Override
    public ContractDTO updateContract(Integer id, Contract updatedContract) {
        // Tìm hợp đồng hiện có
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));

        // Cập nhật các trường
        existingContract.setAmount(updatedContract.getAmount());
        existingContract.setContractDate(updatedContract.getContractDate());

        // Cập nhật thông tin khách hàng nếu có
        if (updatedContract.getCustomer() != null) {
            Customer customer = customerRepository.findById(updatedContract.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + updatedContract.getCustomer().getId()));
            existingContract.setCustomer(customer);
        }

        // Lưu hợp đồng
        Contract savedContract = contractRepository.save(existingContract);

        // Fetch lại hợp đồng với đầy đủ thông tin khách hàng
        Contract fullContract = contractRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new RuntimeException("Updated contract not found"));

        // Chuyển đổi sang ContractDTO và trả về
        return new ContractDTO(
                fullContract.getId(),
                fullContract.getAmount(),
                fullContract.getContractDate(),
                fullContract.getCustomer() != null ? fullContract.getCustomer().getId() : null,
                fullContract.getCustomer() != null ? fullContract.getCustomer().getName() : "N/A"
        );
    }


}
