package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.entity.DocumentSnapshot;
import com.collabdoc.service.VersionService;
import com.collabdoc.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents/{docId}/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveVersion(
            @PathVariable Long docId,
            @RequestBody VersionRequest request) {
        SecurityUtil.getCurrentUserId();
        versionService.saveVersion(docId, request.getContent());
        return ResponseEntity.ok(ApiResponse.ok(null, "Version saved"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentSnapshot>>> getVersions(@PathVariable Long docId) {
        SecurityUtil.getCurrentUserId();
        List<DocumentSnapshot> versions = versionService.getVersions(docId);
        return ResponseEntity.ok(ApiResponse.ok(versions));
    }

    @GetMapping("/{version}")
    public ResponseEntity<ApiResponse<DocumentSnapshot>> getVersion(
            @PathVariable Long docId,
            @PathVariable int version) {
        SecurityUtil.getCurrentUserId();
        DocumentSnapshot snapshot = versionService.getVersion(docId, version);
        return ResponseEntity.ok(ApiResponse.ok(snapshot));
    }

    @Data
    static class VersionRequest {
        private String content;
    }
}
