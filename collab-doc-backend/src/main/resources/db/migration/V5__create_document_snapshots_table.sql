CREATE TABLE document_snapshots (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    yjs_state BYTEA,
    content JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doc_snapshots_doc_id ON document_snapshots(doc_id);
CREATE INDEX idx_doc_snapshots_doc_version ON document_snapshots(doc_id, version DESC);
