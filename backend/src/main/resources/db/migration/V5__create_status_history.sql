CREATE TABLE work_order_status_history (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(20),
    new_status      VARCHAR(20) NOT NULL,
    changed_by_id   BIGINT REFERENCES users(id),
    note            VARCHAR(500),
    changed_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wosh_work_order_id ON work_order_status_history(work_order_id);
