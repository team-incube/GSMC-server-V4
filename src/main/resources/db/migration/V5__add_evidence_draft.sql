ALTER TABLE evidence_tb
    ADD COLUMN is_draft BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_evidence_user_draft_created_at
    ON evidence_tb (user_id, is_draft, evidence_created_at);
