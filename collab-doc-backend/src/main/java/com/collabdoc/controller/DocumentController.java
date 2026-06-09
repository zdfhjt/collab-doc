package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.dto.document.*;
import com.collabdoc.service.DocumentService;
import com.collabdoc.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> create(
            @PathVariable Long workspaceId,
            @RequestBody DocumentCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        DocumentResponse response = documentService.createDocument(workspaceId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Document created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentTreeResponse>>> getTree(
            @PathVariable Long workspaceId) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<DocumentTreeResponse> tree = documentService.getDocumentTree(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(tree));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> get(
            @PathVariable Long workspaceId,
            @PathVariable Long documentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        DocumentResponse response = documentService.getDocument(workspaceId, documentId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> update(
            @PathVariable Long workspaceId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        DocumentResponse response = documentService.updateDocument(workspaceId, documentId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Document updated"));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long workspaceId,
            @PathVariable Long documentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        documentService.deleteDocument(workspaceId, documentId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Document deleted"));
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDelete(
            @PathVariable Long workspaceId,
            @RequestBody List<Long> documentIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        documentService.batchDeleteDocuments(workspaceId, documentIds, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Documents deleted"));
    }

    @PutMapping("/{documentId}/move")
    public ResponseEntity<ApiResponse<DocumentResponse>> move(
            @PathVariable Long workspaceId,
            @PathVariable Long documentId,
            @RequestParam(required = false) Long newParentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        DocumentResponse response = documentService.moveDocument(workspaceId, documentId, newParentId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Document moved"));
    }
}
