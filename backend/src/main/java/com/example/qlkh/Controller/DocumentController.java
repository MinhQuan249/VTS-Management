package com.example.qlkh.Controller;

import com.example.qlkh.Service.DocumentService;
import com.example.qlkh.dto.DocumentDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Value("${ocr.service.url}")
    private String ocrServiceUrl;

    @Value("${file.storage.path}")
    private String fileStoragePath;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        List<DocumentDTO> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "authors", required = false) String authorsJson
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không hợp lệ");
            }

            String originalFileName = file.getOriginalFilename();
            if (!isSupportedFileType(originalFileName)) {
                return ResponseEntity.badRequest().body("File định dạng không được hỗ trợ");
            }

            // Lưu file tạm
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), getFileExtension(originalFileName));
            file.transferTo(tempFile);

            // Gửi file đến dịch vụ OCR
            String extractedText = callOcrService(tempFile);

            // Tạo DTO
            DocumentDTO documentDTO = new DocumentDTO();
            documentDTO.setFileName(originalFileName);
            documentDTO.setFilePath(tempFile.getAbsolutePath());
            documentDTO.setAuthorIds(parseAuthors(authorsJson));
            documentDTO.setExtractedText(extractedText);

            DocumentDTO savedDocument = documentService.uploadDocument(documentDTO);

            return ResponseEntity.ok(savedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi upload tài liệu: " + e.getMessage());
        }
    }

    private List<Integer> parseAuthors(String authorsJson) {
        try {
            if (authorsJson == null || authorsJson.isEmpty()) {
                return List.of(); // Trả về danh sách rỗng nếu không có authors
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(authorsJson, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi parse authors: " + authorsJson, e);
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
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                // Parse JSON results from OCR API
                List<Map<String, Object>> results = mapper.readValue(
                        mapper.readTree(response.getBody()).get("results").toString(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                // Concatenate all texts from results into a single string
                StringBuilder extractedText = new StringBuilder();
                for (Map<String, Object> result : results) {
                    extractedText.append(result.get("text").toString()).append("\n");
                }
                return extractedText.toString().trim();
            } else {
                throw new RuntimeException("OCR Service returned an error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi OCR Service: " + e.getMessage(), e);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        try {
            String filePath = documentService.getDocumentFilePathById(id);
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

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
