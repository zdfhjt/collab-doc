package com.collabdoc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.collabdoc.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@TableName("workspace_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workspace_id")
    private Long workspaceId;

    @TableField("user_id")
    private Long userId;

    private Role role;

    @TableField(value = "joined_at", fill = FieldFill.INSERT)
    private Instant joinedAt;
}
