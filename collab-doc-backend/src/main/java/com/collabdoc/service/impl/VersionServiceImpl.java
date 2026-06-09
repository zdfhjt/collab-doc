package com.collabdoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.collabdoc.entity.DocumentSnapshot;
import com.collabdoc.exception.ResourceNotFoundException;
import com.collabdoc.repository.DocumentSnapshotRepository;
import com.collabdoc.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final DocumentSnapshotRepository snapshotRepository;

    @Override
    public List<DocumentSnapshot> getVersions(Long docId) {
        return snapshotRepository.selectList(
                new LambdaQueryWrapper<DocumentSnapshot>()
                        .eq(DocumentSnapshot::getDocId, docId)
                        .orderByDesc(DocumentSnapshot::getVersion));
    }

    @Override
    public DocumentSnapshot getVersion(Long docId, int version) {
        DocumentSnapshot snapshot = snapshotRepository.selectOne(
                new LambdaQueryWrapper<DocumentSnapshot>()
                        .eq(DocumentSnapshot::getDocId, docId)
                        .eq(DocumentSnapshot::getVersion, version));
        if (snapshot == null) {
            throw new ResourceNotFoundException("Version not found");
        }
        return snapshot;
    }

    @Override
    @Transactional
    public void saveVersion(Long docId, String content) {
        int nextVersion = snapshotRepository.getMaxVersion(docId) + 1;
        DocumentSnapshot snapshot = DocumentSnapshot.builder()
                .docId(docId)
                .version(nextVersion)
                .content(content)
                .build();
        snapshotRepository.insert(snapshot);
    }
}
