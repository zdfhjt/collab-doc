package com.collabdoc.dto.document;

import com.collabdoc.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DocumentTreeResponse {

    private Long id;
    private String title;
    private DocumentType docType;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private List<DocumentTreeResponse> children;
}
