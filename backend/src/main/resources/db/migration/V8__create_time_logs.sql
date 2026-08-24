CREATE TABLE time_logs (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    technician_id   BIGINT NOT NULL REFERENCES users(id),
    minutes         INTEGER NOT NULL CHECK (minutes > 0),
    note            VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_time_logs_work_order ON time_logs(work_order_id);
