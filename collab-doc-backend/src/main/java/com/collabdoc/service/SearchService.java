package com.collabdoc.service;

import com.collabdoc.entity.DocumentIndex;

import java.util.List;

public interface SearchService {

    void indexDocument(Long docId, String title, String content, Long workspaceId);

    void removeDocument(Long docId);

    List<DocumentIndex> search(Long workspaceId, String keyword);
}
