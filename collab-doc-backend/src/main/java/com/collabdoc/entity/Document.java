package com.collabdoc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.collabdoc.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@TableName(value = "documents", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workspace_id")
    private Long workspaceId;

    @TableField("parent_id")
    private Long parentId;

    @Builder.Default
    private String title = "Untitled";

    @TableField("doc_type")
    @Builder.Default
    private DocumentType docType = DocumentType.DOCUMENT;

    @TableField("content")
    private String content;

    @TableField(value = "doc_tree", typeHandler = JacksonTypeHandler.class)
    private String docTree;

    private String icon;

    @TableField("sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
