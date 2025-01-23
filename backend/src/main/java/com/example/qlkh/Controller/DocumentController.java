package com.example.qlkh.Controller;

import com.example.qlkh.Service.DocumentService;
import com.example.qlkh.dto.DocumentDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.qlkh.Controller.OCRController.logger;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Value("${ocr.service.url}")
    private String ocrServiceUrl;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        List<DocumentDTO> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocuments(@RequestParam("files") List<MultipartFile> files,
                                             @RequestParam(value = "authors", required = false) String authorsJson) {
        try {
            // Kiểm tra danh sách tệp
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có tệp nào được tải lên");
            }

            List<DocumentDTO> savedDocuments = new ArrayList<>();

            // Lặp qua từng tệp trong danh sách
            for (MultipartFile file : files) {
                logger.info("Processing file: {}", file.getOriginalFilename());
                if (file.isEmpty()) {
                    logger.warn("File is empty: {}", file.getOriginalFilename());
                    continue;
                }

                String originalFileName = file.getOriginalFilename();
                logger.info("Original file name: {}", originalFileName);
                if (!isSupportedFileType(originalFileName)) {
                    logger.warn("Unsupported file type: {}", originalFileName);
                    continue;
                }

                File tempFile = File.createTempFile(UUID.randomUUID().toString(), getFileExtension(originalFileName));
                file.transferTo(tempFile);

                String extractedText = callOcrService(tempFile);
                logger.info("Extracted text for file {}: {}", originalFileName, extractedText);

                DocumentDTO documentDTO = new DocumentDTO();
                documentDTO.setFileName(originalFileName);
                documentDTO.setFilePath(tempFile.getAbsolutePath());
                documentDTO.setAuthorIds(parseAuthors(authorsJson));
                documentDTO.setExtractedText(extractedText);

                DocumentDTO savedDocument = documentService.uploadDocument(documentDTO);
                logger.info("Document saved with ID: {}", savedDocument.getId());
                savedDocuments.add(savedDocument);
                tempFile.delete();
                logger.info("TempFile deleted!");
            }

            if (savedDocuments.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có tệp hợp lệ được tải lên");
            }

            return ResponseEntity.ok(savedDocuments);
        } catch (Exception e) {
            logger.error("Lỗi khi tải lên tài liệu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tải lên tài liệu: " + e.getMessage());
        }
    }


    private List<Integer> parseAuthors(String authorsJson) {
        try {
            if (authorsJson == null || authorsJson.isEmpty()) {
                return List.of();
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(authorsJson, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing authors", e);
        }
    }

    private boolean isSupportedFileType(String fileName) {
        return fileName.endsWith(".pdf") || fileName.endsWith(".docx") || fileName.endsWith(".doc")
                || fileName.endsWith(".txt") || fileName.endsWith(".png") || fileName.endsWith(".jpg");
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return lastIndex != -1 ? fileName.substring(lastIndex) : "";
    }

    private String callOcrService(File file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(ocrServiceUrl + "/upload", requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> results = mapper.readValue(
                        mapper.readTree(response.getBody()).get("results").toString(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                StringBuilder extractedText = new StringBuilder();
                for (Map<String, Object> result : results) {
                    extractedText.append(result.get("text").toString()).append("\n");
                }
                return extractedText.toString().trim();
            } else {
                throw new RuntimeException("OCR Service error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling OCR Service", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        try {
            boolean deleted = documentService.deleteDocument(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentDetails(@PathVariable Integer id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
