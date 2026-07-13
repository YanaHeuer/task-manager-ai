CREATE TABLE tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    ai_summary  TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'TODO',
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE INDEX idx_tasks_status ON tasks (status);
