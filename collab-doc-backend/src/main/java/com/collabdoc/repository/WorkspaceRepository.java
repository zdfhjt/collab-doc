package com.collabdoc.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.Workspace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkspaceRepository extends BaseMapper<Workspace> {

    @Select("""
        SELECT w.* FROM workspaces w
        WHERE w.owner_id = #{userId}
           OR w.id IN (SELECT wm.workspace_id FROM workspace_members wm WHERE wm.user_id = #{userId})
        ORDER BY w.updated_at DESC
    """)
    List<Workspace> findAllByUserId(Long userId);
}
