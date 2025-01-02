package com.example.qlkh.Service;

import com.example.qlkh.dto.DocumentDTO;

import java.util.List;

public interface DocumentService {

    // Lấy danh sách tài liệu
    List<DocumentDTO> getAllDocuments();

    // Thêm tài liệu mới
    DocumentDTO uploadDocument(DocumentDTO documentDTO);

    // Xóa tài liệu
    boolean deleteDocument(Integer documentId);

    // Lấy chi tiết tài liệu
    DocumentDTO getDocumentById(Integer documentId);

    // Lấy đường dẫn file để tải xuống
    String getDocumentFilePathById(Integer documentId);
}
