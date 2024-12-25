package com.example.qlkh.Controller;

import com.example.qlkh.Service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private OCRService ocrService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ground_truth", required = false) String groundTruth) {

        // Kiểm tra file trống
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image"))) {
            return ResponseEntity.badRequest().body("Invalid file type. Please upload an image or PDF.");
        }

        try {
            // Gọi service để xử lý OCR
            Map<String, Object> result = ocrService.processOCR(file, groundTruth);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Trả lỗi chi tiết khi xử lý OCR
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing file: " + e.getMessage()));
        }
    }
}
