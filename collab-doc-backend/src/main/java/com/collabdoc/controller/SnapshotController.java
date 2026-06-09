package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.entity.Document;
import com.collabdoc.repository.DocumentRepository;
import com.collabdoc.service.SearchService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class SnapshotController {

    private final DocumentRepository documentRepository;
    private final SearchService searchService;

    /**
     * Called by y-websocket relay every 30 seconds.
     * Only updates documents.content and ES index.
     * Does NOT write to document_snapshots (manual versions only).
     */
    @PutMapping("/{docId}/snapshot")
    public ResponseEntity<ApiResponse<Void>> updateContent(
            @PathVariable Long docId,
            @RequestBody SnapshotRequest request) {
        Document doc = documentRepository.selectById(docId);
        String content = request.getContent();
        log.info("Snapshot for doc {}: content length={}, yjsState length={}",
                docId,
                content != null ? content.length() : 0,
                request.getYjsState() != null ? request.getYjsState().length() : 0);
        if (content != null && content.length() > 50) {
            log.info("Content preview: {}", content.substring(0, Math.min(200, content.length())));
        }
        if (doc != null && content != null && !content.isEmpty() && content.length() > 10) {
            doc.setContent(content);
            documentRepository.updateById(doc);
            searchService.indexDocument(docId, doc.getTitle(), content, doc.getWorkspaceId());
            log.info("Updated content + ES index for doc {}", docId);
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "OK"));
    }

    @Data
    static class SnapshotRequest {
        private String yjsState;
        private String content;
    }
}
