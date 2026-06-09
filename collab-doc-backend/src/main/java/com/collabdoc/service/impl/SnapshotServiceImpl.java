package com.collabdoc.service.impl;

import com.collabdoc.entity.DocumentSnapshot;
import com.collabdoc.repository.DocumentSnapshotRepository;
import com.collabdoc.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final DocumentSnapshotRepository snapshotRepository;

    @Override
    @Transactional
    public void saveSnapshot(Long docId, String yjsStateBase64, String contentJson) {
        byte[] yjsState = Base64.getDecoder().decode(yjsStateBase64);
        int nextVersion = snapshotRepository.getMaxVersion(docId) + 1;

        DocumentSnapshot snapshot = DocumentSnapshot.builder()
                .docId(docId)
                .version(nextVersion)
                .yjsState(yjsState)
                .content(contentJson)
                .build();
        snapshotRepository.insert(snapshot);

        log.info("Saved snapshot v{} for doc {}", nextVersion, docId);
    }

    @Override
    public DocumentSnapshot getLatestSnapshot(Long docId) {
        return snapshotRepository.findLatestByDocId(docId);
    }

    @Override
    public String getYjsStateBase64(Long docId) {
        DocumentSnapshot snapshot = snapshotRepository.findLatestByDocId(docId);
        if (snapshot == null || snapshot.getYjsState() == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(snapshot.getYjsState());
    }
}
