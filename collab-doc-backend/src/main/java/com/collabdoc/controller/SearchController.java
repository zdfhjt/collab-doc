package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.entity.Document;
import com.collabdoc.entity.DocumentIndex;
import com.collabdoc.repository.DocumentRepository;
import com.collabdoc.service.SearchService;
import com.collabdoc.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final DocumentRepository documentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentIndex>>> search(
            @RequestParam Long workspaceId,
            @RequestParam String keyword) {
        SecurityUtil.getCurrentUserId();
        List<DocumentIndex> results = searchService.search(workspaceId, keyword);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindex() {
        List<Document> docs = documentRepository.selectList(null);
        int count = 0;
        for (Document doc : docs) {
            searchService.indexDocument(doc.getId(), doc.getTitle(), doc.getContent(), doc.getWorkspaceId());
            count++;
        }
        return ResponseEntity.ok(ApiResponse.ok("Indexed " + count + " documents"));
    }

    @GetMapping("/debug/{docId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> debugDoc(@PathVariable Long docId) {
        Document doc = documentRepository.selectById(docId);
        String content = doc != null ? doc.getContent() : "null";
        String preview = content != null ? content.substring(0, Math.min(200, content.length())) : "empty";
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "docId", String.valueOf(docId),
                "title", doc != null ? doc.getTitle() : "null",
                "contentLength", content != null ? String.valueOf(content.length()) : "0",
                "contentPreview", preview
        )));
    }
}
