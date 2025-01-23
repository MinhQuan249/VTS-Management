package com.example.qlkh.Service.Impl;

import com.example.qlkh.Entity.Customer;
import com.example.qlkh.Entity.Document;
import com.example.qlkh.Repository.CustomerRepository;
import com.example.qlkh.Repository.DocumentRepository;
import com.example.qlkh.Service.DocumentService;
import com.example.qlkh.dto.DocumentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.qlkh.Controller.OCRController.logger;

@Transactional
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Value("${ocr.service.url}")
    private String ocrServiceUrl;

    @Override
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO uploadDocument(DocumentDTO documentDTO) {
        Document document = new Document();
        document.setFileName(documentDTO.getFileName());
        document.setFilePath(documentDTO.getFilePath());
        document.setExtractedText(documentDTO.getExtractedText());
        document.setCreatedAt(LocalDateTime.now());

        List<Integer> authorIds = documentDTO.getAuthorIds();
        if (authorIds != null && !authorIds.isEmpty()) {
            List<Customer> authors = customerRepository.findAllById(authorIds);
            document.setAuthors(authors);
        } else {
            document.setAuthors(Collections.emptyList()); // Nếu không có tác giả
        }
        logger.info("Attempting to save document: {}", document);
        Document savedDocument = documentRepository.save(document);
        logger.info("Document saved successfully with ID: {}", savedDocument.getId());


        return new DocumentDTO(
                savedDocument.getId(),
                savedDocument.getAuthors().stream().map(Customer::getId).collect(Collectors.toList()),
                savedDocument.getFileName(),
                savedDocument.getFilePath(),
                savedDocument.getExtractedText(),
                savedDocument.getCreatedAt().toString()
        );
    }

    @Override
    public boolean deleteDocument(Integer documentId) {
        if (!documentRepository.existsById(documentId)) {
            return false;
        }
        documentRepository.deleteById(documentId);
        return true;
    }

    @Override
    public DocumentDTO getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return convertToDTO(document);
    }

    @Override
    public String getDocumentFilePathById(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        return document.getFilePath();
    }


    private DocumentDTO convertToDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getAuthors().stream()
                        .map(Customer::getId)
                        .collect(Collectors.toList()),
                document.getFileName(),
                document.getFilePath(),
                document.getExtractedText(),
                document.getCreatedAt().toString()
        );
    }
}
