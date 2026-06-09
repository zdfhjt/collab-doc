package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.service.FileStorageService;
import com.collabdoc.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UploadResult>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("workspaceId") Long workspaceId) {
        SecurityUtil.getCurrentUserId();
        String objectKey = fileStorageService.uploadFile(file, workspaceId);
        String url = fileStorageService.getFileUrl(objectKey);
        return ResponseEntity.ok(ApiResponse.ok(new UploadResult(objectKey, url)));
    }

    @GetMapping("/url/{*objectKey}")
    public ResponseEntity<ApiResponse<String>> getUrl(@PathVariable String objectKey) {
        SecurityUtil.getCurrentUserId();
        String url = fileStorageService.getFileUrl(objectKey);
        return ResponseEntity.ok(ApiResponse.ok(url));
    }

    @PostMapping("/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestBody ProxyRequest req) {
        try {
            String imageUrl = req.getUrl();
            log.info("Proxying image: {}", imageUrl.substring(0, Math.min(imageUrl.length(), 80)));

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    imageUrl, HttpMethod.GET, null, byte[].class);

            HttpHeaders headers = new HttpHeaders();
            String contentType = response.getHeaders().getContentType() != null
                    ? response.getHeaders().getContentType().toString()
                    : "image/png";
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @PostMapping("/import-external")
    public ResponseEntity<ApiResponse<String>> importExternal(
            @RequestBody ProxyRequest req,
            @RequestParam Long workspaceId) {
        SecurityUtil.getCurrentUserId();
        String minioUrl = fileStorageService.importExternalImage(req.getUrl(), workspaceId);
        return ResponseEntity.ok(ApiResponse.ok(minioUrl));
    }

    @Data
    static class ProxyRequest {
        private String url;
    }

    @Data
    static class UploadResult {
        private final String objectKey;
        private final String url;
    }
}
