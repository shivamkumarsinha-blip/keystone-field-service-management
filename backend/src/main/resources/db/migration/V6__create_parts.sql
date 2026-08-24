CREATE TABLE parts (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    sku             VARCHAR(60)  NOT NULL UNIQUE,
    quantity_in_stock INTEGER    NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    unit_cost       NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (unit_cost >= 0),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
