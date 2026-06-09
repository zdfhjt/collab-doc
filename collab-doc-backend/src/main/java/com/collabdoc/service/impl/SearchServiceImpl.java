package com.collabdoc.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.collabdoc.entity.DocumentIndex;
import com.collabdoc.repository.DocumentRepository;
import com.collabdoc.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient esClient;
    private final DocumentRepository documentRepository;

    private static final String INDEX = "documents";

    @Override
    public void indexDocument(Long docId, String title, String content, Long workspaceId) {
        try {
            String plainContent = extractTextFromContent(content);
            log.info("Indexing doc {}: contentPreview={}, plainText={}",
                    docId,
                    content != null ? content.substring(0, Math.min(300, content.length())) : "null",
                    plainContent.substring(0, Math.min(100, plainContent.length())));;

            Map<String, Object> doc = new HashMap<>();
            doc.put("title", title != null ? title : "");
            doc.put("content", plainContent);
            doc.put("workspaceId", workspaceId);
            doc.put("docType", "DOCUMENT");

            esClient.index(i -> i
                    .index(INDEX)
                    .id(String.valueOf(docId))
                    .document(doc));

            log.info("Indexed doc {} (content length: {})", docId, plainContent.length());
        } catch (Exception e) {
            log.warn("Failed to index doc {}: {}", docId, e.getMessage());
        }
    }

    @Override
    public void removeDocument(Long docId) {
        try {
            esClient.delete(d -> d.index(INDEX).id(String.valueOf(docId)));
        } catch (Exception e) {
            log.warn("Failed to remove doc {}: {}", docId, e.getMessage());
        }
    }

    @Override
    public List<DocumentIndex> search(Long workspaceId, String keyword) {
        try {
            SearchResponse<DocumentIndex> response = esClient.search(s -> s
                    .index(INDEX)
                    .query(q -> q.bool(b -> b
                            .should(sh -> sh.matchPhrase(mp -> mp
                                    .field("title")
                                    .query(keyword)
                                    .boost(3.0f)))
                            .should(sh -> sh.matchPhrase(mp -> mp
                                    .field("content")
                                    .query(keyword)))
                            .minimumShouldMatch("1")
                            .filter(f -> f.term(t -> t
                                    .field("workspaceId")
                                    .value(workspaceId)))))
                    .size(20),
                    DocumentIndex.class);

            return response.hits().hits().stream()
                    .map(hit -> {
                        DocumentIndex doc = hit.source();
                        if (doc != null) {
                            doc.setId(Long.parseLong(hit.id()));
                        }
                        return doc;
                    })
                    .filter(doc -> doc != null)
                    .toList();
        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractTextFromContent(String content) {
        if (content == null || content.isBlank()) return "";
        // Strip all XML/HTML tags to get plain text
        String text = content.replaceAll("<[^>]+>", " ").replaceAll("\"", "").trim();
        // Collapse multiple spaces
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }
}
