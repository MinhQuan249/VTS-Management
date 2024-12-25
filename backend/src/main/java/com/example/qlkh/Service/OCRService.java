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

import java.util.HashMap;
import java.util.Map;

@Service
public class OCRService {

    private final RestTemplate restTemplate;

    @Value("${ocr.service.url}")
    private String ocrServiceUrl;

    public OCRService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> processOCR(MultipartFile file, String groundTruth) {
        try {
            // Cấu hình header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Tạo body gửi đi
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            if (groundTruth != null) {
                body.add("ground_truth", groundTruth);
            }

            // Tạo request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Gửi request tới OCR Service
            ResponseEntity<Map> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, Map.class);

            // Trả kết quả từ OCR Service
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // Xử lý lỗi HTTP từ OCR Service
            return Map.of("error", "OCR service returned error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // Xử lý lỗi khác (như kết nối)
            return Map.of("error", "Failed to call OCR service: " + e.getMessage());
        }
    }
}
