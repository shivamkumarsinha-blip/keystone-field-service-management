CREATE TABLE part_usage (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    part_id         BIGINT NOT NULL REFERENCES parts(id),
    technician_id   BIGINT REFERENCES users(id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost_at_use NUMERIC(12,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_part_usage_work_order ON part_usage(work_order_id);
