package com.collabdoc.config;

import com.collabdoc.entity.Document;
import com.collabdoc.entity.DocumentSnapshot;
import com.collabdoc.repository.DocumentRepository;
import com.collabdoc.repository.DocumentSnapshotRepository;
import com.collabdoc.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexInitializer implements ApplicationRunner {

    private final DocumentRepository documentRepository;
    private final DocumentSnapshotRepository snapshotRepository;
    private final SearchService searchService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Search index init ===");
        try {
            List<Document> docs = documentRepository.selectList(null);
            log.info("Found {} documents", docs.size());
            int count = 0;
            for (Document doc : docs) {
                try {
                    // Get content: prefer documents.content, fallback to latest snapshot
                    String content = doc.getContent();
                    if (content == null || content.isEmpty()) {
                        DocumentSnapshot snapshot = snapshotRepository.findLatestByDocId(doc.getId());
                        if (snapshot != null) {
                            content = snapshot.getContent();
                        }
                    }
                    searchService.indexDocument(doc.getId(), doc.getTitle(), content, doc.getWorkspaceId());
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to index doc {}: {}", doc.getId(), e.getMessage());
                }
            }
            log.info("=== Indexed {} documents ===", count);
        } catch (Exception e) {
            log.error("=== Init failed: {} ===", e.getMessage(), e);
        }
    }
}
