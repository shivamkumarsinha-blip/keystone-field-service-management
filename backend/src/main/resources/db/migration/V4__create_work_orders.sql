CREATE TABLE work_orders (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(30) NOT NULL UNIQUE,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT,
    priority                VARCHAR(20) NOT NULL CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    status                  VARCHAR(20) NOT NULL CHECK (status IN
                                ('NEW','ASSIGNED','IN_PROGRESS','ON_HOLD','COMPLETED','CLOSED','CANCELLED')),
    customer_id             BIGINT NOT NULL REFERENCES customers(id),
    site_id                 BIGINT NOT NULL REFERENCES sites(id),
    assigned_technician_id  BIGINT REFERENCES users(id),
    created_by_id           BIGINT REFERENCES users(id),
    sla_due_at              TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    started_at              TIMESTAMP,
    completed_at            TIMESTAMP,
    closed_at               TIMESTAMP
);

CREATE INDEX idx_wo_status ON work_orders(status);
CREATE INDEX idx_wo_assigned_technician ON work_orders(assigned_technician_id);
CREATE INDEX idx_wo_customer ON work_orders(customer_id);
CREATE INDEX idx_wo_site ON work_orders(site_id);
CREATE INDEX idx_wo_sla_due_at ON work_orders(sla_due_at);

-- sequence backing the human-readable WO-YYYY-NNNNNN code, reset logic handled in application code
CREATE SEQUENCE work_order_code_seq START WITH 1;
