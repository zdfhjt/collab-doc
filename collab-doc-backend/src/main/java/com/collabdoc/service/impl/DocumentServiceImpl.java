package com.collabdoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.collabdoc.dto.document.*;
import com.collabdoc.entity.Document;
import com.collabdoc.entity.WorkspaceMember;
import com.collabdoc.exception.ResourceNotFoundException;
import com.collabdoc.repository.DocumentRepository;
import com.collabdoc.repository.WorkspaceMemberRepository;
import com.collabdoc.service.DocumentService;
import com.collabdoc.service.DocumentTreeCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final DocumentTreeCacheService cacheService;
    private final com.collabdoc.service.SearchService searchService;
    private final com.collabdoc.service.VersionService versionService;

    @Override
    @Transactional
    public DocumentResponse createDocument(Long workspaceId, DocumentCreateRequest request, Long userId) {
        checkMember(workspaceId, userId);

        Long parentId = (request.getParentId() != null && request.getParentId() > 0)
                ? request.getParentId() : null;

        Document doc = Document.builder()
                .workspaceId(workspaceId)
                .parentId(parentId)
                .title(request.getTitle())
                .docType(request.getDocType())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .createdBy(userId)
                .build();

        documentRepository.insert(doc);
        cacheService.invalidate(workspaceId);
        searchService.indexDocument(doc.getId(), doc.getTitle(), doc.getContent(), workspaceId);
        return toResponse(doc);
    }

    @Override
    public DocumentResponse getDocument(Long workspaceId, Long documentId, Long userId) {
        checkMember(workspaceId, userId);
        Document doc = documentRepository.selectById(documentId);
        if (doc == null) {
            throw new ResourceNotFoundException("Document not found");
        }
        return toResponse(doc);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long workspaceId, Long documentId,
                                           DocumentUpdateRequest request, Long userId) {
        checkMember(workspaceId, userId);
        Document doc = documentRepository.selectById(documentId);
        if (doc == null) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getContent() != null) doc.setContent(request.getContent());
        if (request.getIcon() != null) doc.setIcon(request.getIcon());
        if (request.getSortOrder() != null) doc.setSortOrder(request.getSortOrder());

        documentRepository.updateById(doc);
        cacheService.invalidate(workspaceId);
        searchService.indexDocument(doc.getId(), doc.getTitle(), doc.getContent(), workspaceId);
        return toResponse(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(Long workspaceId, Long documentId, Long userId) {
        checkMember(workspaceId, userId);
        Document doc = documentRepository.selectById(documentId);
        if (doc == null) {
            throw new ResourceNotFoundException("Document not found");
        }
        deleteChildren(documentId);
        documentRepository.deleteById(documentId);
        cacheService.invalidate(workspaceId);
        searchService.removeDocument(documentId);
    }

    @Override
    @Transactional
    public void batchDeleteDocuments(Long workspaceId, List<Long> documentIds, Long userId) {
        checkMember(workspaceId, userId);
        for (Long documentId : documentIds) {
            Document doc = documentRepository.selectById(documentId);
            if (doc == null) continue;
            deleteChildren(documentId);
            documentRepository.deleteById(documentId);
            searchService.removeDocument(documentId);
        }
        cacheService.invalidate(workspaceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DocumentTreeResponse> getDocumentTree(Long workspaceId, Long userId) {
        checkMember(workspaceId, userId);

        List<Map<String, Object>> cached = cacheService.getOrLoad(workspaceId, () -> {
            List<Document> allDocs = documentRepository.selectList(
                    new LambdaQueryWrapper<Document>()
                            .select(Document::getId, Document::getTitle, Document::getDocType,
                                    Document::getIcon, Document::getParentId, Document::getSortOrder)
                            .eq(Document::getWorkspaceId, workspaceId));
            return treeToMaps(buildTree(allDocs, null));
        });

        if (cached == null) return List.of();
        return convertFromCached(cached);
    }

    @Override
    @Transactional
    public DocumentResponse moveDocument(Long workspaceId, Long documentId,
                                          Long newParentId, Long userId) {
        checkMember(workspaceId, userId);
        Document doc = documentRepository.selectById(documentId);
        if (doc == null) {
            throw new ResourceNotFoundException("Document not found");
        }
        doc.setParentId(newParentId);
        documentRepository.updateById(doc);
        cacheService.invalidate(workspaceId);
        return toResponse(doc);
    }

    private void deleteChildren(Long parentId) {
        List<Document> children = documentRepository.selectList(
                new LambdaQueryWrapper<Document>().eq(Document::getParentId, parentId));
        for (Document child : children) {
            deleteChildren(child.getId());
            documentRepository.deleteById(child.getId());
        }
    }

    private List<DocumentTreeResponse> buildTree(List<Document> docs, Long parentId) {
        List<Document> children = docs.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .sorted(Comparator.comparing(Document::getSortOrder)
                        .thenComparing(Document::getId))
                .toList();

        return children.stream()
                .map(doc -> DocumentTreeResponse.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .docType(doc.getDocType())
                        .icon(doc.getIcon())
                        .parentId(doc.getParentId())
                        .sortOrder(doc.getSortOrder())
                        .children(buildTree(docs, doc.getId()))
                        .build())
                .toList();
    }

    private DocumentResponse toResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .workspaceId(doc.getWorkspaceId())
                .parentId(doc.getParentId())
                .title(doc.getTitle())
                .docType(doc.getDocType())
                .content(doc.getContent())
                .icon(doc.getIcon())
                .sortOrder(doc.getSortOrder())
                .createdBy(doc.getCreatedBy())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private List<Map<String, Object>> treeToMaps(List<DocumentTreeResponse> tree) {
        return tree.stream().map(node -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", node.getId());
            map.put("title", node.getTitle());
            map.put("docType", node.getDocType().name());
            map.put("icon", node.getIcon());
            map.put("parentId", node.getParentId());
            map.put("sortOrder", node.getSortOrder());
            map.put("children", treeToMaps(node.getChildren()));
            return map;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<DocumentTreeResponse> convertFromCached(List<Map<String, Object>> cached) {
        return cached.stream().map(map -> DocumentTreeResponse.builder()
                .id(((Number) map.get("id")).longValue())
                .title((String) map.get("title"))
                .docType(com.collabdoc.enums.DocumentType.valueOf((String) map.get("docType")))
                .icon((String) map.get("icon"))
                .parentId(map.get("parentId") != null ? ((Number) map.get("parentId")).longValue() : null)
                .sortOrder(map.get("sortOrder") != null ? ((Number) map.get("sortOrder")).intValue() : 0)
                .children(map.get("children") != null
                        ? convertFromCached((List<Map<String, Object>>) map.get("children"))
                        : List.of())
                .build()).toList();
    }

    private void checkMember(Long workspaceId, Long userId) {
        Long count = memberRepository.selectCount(
                new LambdaQueryWrapper<WorkspaceMember>()
                        .eq(WorkspaceMember::getWorkspaceId, workspaceId)
                        .eq(WorkspaceMember::getUserId, userId));
        if (count == 0) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }
    }
}
