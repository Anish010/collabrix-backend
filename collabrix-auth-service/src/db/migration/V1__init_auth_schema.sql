CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE auth_outbox_events (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,

    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,

    last_error TEXT,
    sent_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_outbox_status_created
    ON auth_outbox_events(status, created_at);