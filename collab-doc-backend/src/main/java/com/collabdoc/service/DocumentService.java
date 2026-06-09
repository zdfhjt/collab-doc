package com.collabdoc.service;

import com.collabdoc.dto.document.*;

import java.util.List;

public interface DocumentService {

    DocumentResponse createDocument(Long workspaceId, DocumentCreateRequest request, Long userId);

    DocumentResponse getDocument(Long workspaceId, Long documentId, Long userId);

    DocumentResponse updateDocument(Long workspaceId, Long documentId, DocumentUpdateRequest request, Long userId);

    void deleteDocument(Long workspaceId, Long documentId, Long userId);

    void batchDeleteDocuments(Long workspaceId, List<Long> documentIds, Long userId);

    List<DocumentTreeResponse> getDocumentTree(Long workspaceId, Long userId);

    DocumentResponse moveDocument(Long workspaceId, Long documentId, Long newParentId, Long userId);
}
