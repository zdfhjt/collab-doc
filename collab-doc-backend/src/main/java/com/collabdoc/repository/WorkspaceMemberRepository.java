package com.collabdoc.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.WorkspaceMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkspaceMemberRepository extends BaseMapper<WorkspaceMember> {
}
