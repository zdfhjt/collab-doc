package com.collabdoc.service;

import com.collabdoc.entity.DocumentSnapshot;

public interface SnapshotService {

    void saveSnapshot(Long docId, String yjsStateBase64, String contentJson);

    DocumentSnapshot getLatestSnapshot(Long docId);

    String getYjsStateBase64(Long docId);
}
