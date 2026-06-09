package com.collabdoc.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.DocumentSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentSnapshotRepository extends BaseMapper<DocumentSnapshot> {

    @Select("SELECT * FROM document_snapshots WHERE doc_id = #{docId} ORDER BY version DESC LIMIT 1")
    DocumentSnapshot findLatestByDocId(Long docId);

    @Select("SELECT COALESCE(MAX(version), 0) FROM document_snapshots WHERE doc_id = #{docId}")
    int getMaxVersion(Long docId);
}
