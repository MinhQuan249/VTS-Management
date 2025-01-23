package com.example.qlkh.Controller;

import com.example.qlkh.Service.OCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    public static final Logger logger = LoggerFactory.getLogger(OCRController.class);

    @Autowired
    private OCRService ocrService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        // Kiểm tra từng file
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image"))) {
                return ResponseEntity.badRequest().body("Invalid file type for file: " + file.getOriginalFilename());
            }
        }

        try {
            // Gọi service để xử lý nhiều file
            List<Map<String, Object>> results = ocrService.processMultipleFiles(files);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error processing files: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing files: " + e.getMessage()));
        }
    }

    @PostMapping("/compare")
    public ResponseEntity<?> compareText(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Request Body: {}", requestBody);
        } catch (Exception e) {
            logger.error("Error logging request: {}", e.getMessage());
        }
        String ocrText = (String) requestBody.get("text");
        List<Map<String, Object>> documents = (List<Map<String, Object>>) requestBody.get("documents");

        if (ocrText == null || ocrText.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Text for comparison is missing");
        }
        if (documents == null || documents.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Documents for comparison are missing");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", ocrText);
            payload.put("documents", documents);

            Map<String, Object> comparisonResults = ocrService.compareWithPython(payload);
            return ResponseEntity.ok(comparisonResults);
        } catch (Exception e) {
            logger.error("Error during comparison: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error during comparison: " + e.getMessage()));
        }
    }
}
