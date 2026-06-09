package com.collabdoc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIndex {

    private Long id;
    private String title;
    private String content;
    private Long workspaceId;
    private String docType;
    private Instant updatedAt;
}
