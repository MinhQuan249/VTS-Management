package com.example.qlkh.Service;

import com.example.qlkh.Entity.Contract;
import com.example.qlkh.dto.ContractDTO;

import java.util.List;

public interface ContractService {
    List<ContractDTO> getAllContracts();
    Contract getContractById(Integer id);
    ContractDTO addContract(ContractDTO contractDTO);
    void deleteContract(Integer id);
    ContractDTO updateContract(Integer id, Contract updatedContract);






}
