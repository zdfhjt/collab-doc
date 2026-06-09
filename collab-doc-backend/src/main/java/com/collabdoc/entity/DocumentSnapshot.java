package com.collabdoc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@TableName("document_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doc_id")
    private Long docId;

    @Builder.Default
    private Integer version = 1;

    @TableField("yjs_state")
    private byte[] yjsState;

    @TableField("content")
    private String content;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
