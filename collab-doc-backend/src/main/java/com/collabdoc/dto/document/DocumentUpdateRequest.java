package com.collabdoc.dto.document;

import lombok.Data;

@Data
public class DocumentUpdateRequest {

    private String title;
    private String content;
    private String icon;
    private Integer sortOrder;
}
