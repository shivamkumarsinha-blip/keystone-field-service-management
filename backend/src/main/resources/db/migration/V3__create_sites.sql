CREATE TABLE sites (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    address_line    VARCHAR(255) NOT NULL,
    city            VARCHAR(120),
    state           VARCHAR(120),
    postal_code     VARCHAR(20),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sites_customer_id ON sites(customer_id);
