CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)  NOT NULL,
    contact_email   VARCHAR(180),
    contact_phone   VARCHAR(40),
    -- a CUSTOMER-role user that logs into the portal for this organization
    portal_user_id  BIGINT REFERENCES users(id),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_customers_portal_user ON customers(portal_user_id);
