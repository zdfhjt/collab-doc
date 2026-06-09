package com.collabdoc.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file, Long workspaceId) {
        try {
            String objectKey = "workspace/" + workspaceId + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Download an external URL and upload to MinIO in one step.
     * Returns the public MinIO URL.
     */
    public String importExternalImage(String imageUrl, Long workspaceId) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(imageUrl))
                    .GET()
                    .build();
            java.net.http.HttpResponse<byte[]> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());

            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            if (fileName.contains("?")) fileName = fileName.substring(0, fileName.indexOf('?'));
            if (fileName.isEmpty()) fileName = "image.png";
            fileName = java.net.URLDecoder.decode(fileName, java.nio.charset.StandardCharsets.UTF_8);

            String objectKey = "workspace/" + workspaceId + "/" + UUID.randomUUID() + "/" + fileName;
            String contentType = response.headers().firstValue("content-type").orElse("image/png");

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(new java.io.ByteArrayInputStream(response.body()), response.body().length, -1)
                    .contentType(contentType)
                    .build());

            return getFileUrl(objectKey);
        } catch (Exception e) {
            log.warn("Failed to import external image {}: {}", imageUrl, e.getMessage());
            return imageUrl; // Return original URL on failure
        }
    }

    public String getFileUrl(String objectKey) {
        // Return direct public URL (bucket is public, no presigned URL needed)
        return endpoint + "/" + bucketName + "/" + objectKey;
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
}
