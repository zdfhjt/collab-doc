package com.collabdoc.dto.document;

import com.collabdoc.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private Long workspaceId;
    private Long parentId;
    private String title;
    private DocumentType docType;
    private String content;
    private String icon;
    private Integer sortOrder;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
