CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL DEFAULT 'Untitled',
    doc_type VARCHAR(20) NOT NULL DEFAULT 'DOCUMENT',
    content TEXT,
    doc_tree JSONB,
    icon VARCHAR(10),
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_workspace_parent ON documents(workspace_id, parent_id);
CREATE INDEX idx_documents_doc_tree ON documents USING GIN(doc_tree);
