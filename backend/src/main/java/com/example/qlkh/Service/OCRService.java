package com.example.qlkh.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.qlkh.Controller.OCRController.logger;

@Service
public class OCRService {

    private final RestTemplate restTemplate;

    @Value("${ocr.service.url}")
    private String ocrServiceUrl; // URL của Flask OCR Service

    public OCRService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Xử lý OCR cho nhiều file tải lên.
     * @param files Danh sách file cần nhận diện.
     * @return Kết quả OCR cho từng file.
     */
    public List<Map<String, Object>> processMultipleFiles(List<MultipartFile> files) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Xử lý OCR từng file
                results.add(processOCR(file));
            } catch (Exception e) {
                logger.error("Error processing file {}: {}", file.getOriginalFilename(), e.getMessage());
                results.add(Map.of(
                        "fileName", file.getOriginalFilename(),
                        "error", e.getMessage()
                ));
            }
        }

        return results;
    }

    /**
     * Xử lý OCR cho file tải lên.
     * @param file File cần nhận diện.
     * @return Kết quả OCR.
     */
    public Map<String, Object> processOCR(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ocrServiceUrl + "/upload",
                    requestEntity,
                    Map.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error from OCR service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of(
                    "fileName", file.getOriginalFilename(),
                    "error", "OCR service error: " + e.getStatusCode()
            );
        } catch (Exception e) {
            logger.error("Failed to process file {}: {}", file.getOriginalFilename(), e.getMessage());
            return Map.of(
                    "fileName", file.getOriginalFilename(),
                    "error", "Failed to process file: " + e.getMessage()
            );
        }
    }
    /**
     * Gửi yêu cầu so sánh văn bản tới Python OCR service.
     * @param payload Payload bao gồm text và danh sách tài liệu.
     * @return Kết quả so sánh từ Python OCR service.
     */
    public Map<String, Object> compareWithPython(Map<String, Object> payload) {
        try {
            // Cấu hình header cho request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tạo request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            // Gửi request tới Flask
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ocrServiceUrl + "/compare",
                    requestEntity,
                    Map.class
            );

            // Trả kết quả từ Flask
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error calling Python OCR service: " + e.getMessage());
        }
    }
}
