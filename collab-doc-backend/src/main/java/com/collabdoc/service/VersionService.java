package com.collabdoc.service;

import com.collabdoc.entity.DocumentSnapshot;

import java.util.List;

public interface VersionService {

    List<DocumentSnapshot> getVersions(Long docId);

    DocumentSnapshot getVersion(Long docId, int version);

    void saveVersion(Long docId, String content);
}
