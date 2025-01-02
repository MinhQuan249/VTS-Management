package com.example.qlkh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Integer id;
    private List<Integer> authorIds;
    private String fileName;
    private String filePath;
    private String extractedText;
    private String createdAt;
}
