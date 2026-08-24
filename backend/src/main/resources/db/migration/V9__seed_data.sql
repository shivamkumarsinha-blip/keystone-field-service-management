-- Development seed data only. Passwords below are all the bcrypt hash of "Password123!"
-- (generated and self-verified offline via libxcrypt's blowfish/bcrypt implementation).
-- DO NOT use these accounts or this hash strategy in production.
INSERT INTO users (full_name, email, password_hash, role) VALUES
  ('Dana Dispatcher', 'dispatcher@example.com', '$2b$12$UJ4BaPPV2utRjcBet2sGSuGCTl1Tx5ipAXQ9Qoly6l1tIt0fjBhgy', 'DISPATCHER'),
  ('Tara Technician',  'technician@example.com', '$2b$12$UJ4BaPPV2utRjcBet2sGSuGCTl1Tx5ipAXQ9Qoly6l1tIt0fjBhgy', 'TECHNICIAN'),
  ('Marcus Manager',   'manager@example.com',    '$2b$12$UJ4BaPPV2utRjcBet2sGSuGCTl1Tx5ipAXQ9Qoly6l1tIt0fjBhgy', 'MANAGER'),
  ('Cara Customer',    'customer@example.com',   '$2b$12$UJ4BaPPV2utRjcBet2sGSuGCTl1Tx5ipAXQ9Qoly6l1tIt0fjBhgy', 'CUSTOMER');

INSERT INTO customers (name, contact_email, contact_phone, portal_user_id)
VALUES ('Acme Facilities Inc.', 'ops@acme-facilities.example', '+1-555-0100',
        (SELECT id FROM users WHERE email = 'customer@example.com'));

INSERT INTO sites (customer_id, name, address_line, city, state, postal_code)
VALUES
  ((SELECT id FROM customers WHERE name = 'Acme Facilities Inc.'), 'Acme HQ', '100 Main St', 'Springfield', 'IL', '62701'),
  ((SELECT id FROM customers WHERE name = 'Acme Facilities Inc.'), 'Acme Warehouse 2', '450 Industrial Pkwy', 'Springfield', 'IL', '62702');

INSERT INTO parts (name, sku, quantity_in_stock, unit_cost) VALUES
  ('HVAC Filter 20x20', 'PRT-1001', 50, 12.50),
  ('Compressor Belt', 'PRT-1002', 20, 34.00),
  ('Thermostat Unit', 'PRT-1003', 15, 89.99);

INSERT INTO work_orders (code, title, description, priority, status, customer_id, site_id, created_by_id, sla_due_at)
VALUES (
  'WO-2026-000001',
  'AC unit not cooling',
  'Rooftop AC unit on building A is blowing warm air.',
  'HIGH',
  'NEW',
  (SELECT id FROM customers WHERE name = 'Acme Facilities Inc.'),
  (SELECT id FROM sites WHERE name = 'Acme HQ'),
  (SELECT id FROM users WHERE email = 'dispatcher@example.com'),
  now() + interval '24 hours'
);

INSERT INTO work_order_status_history (work_order_id, previous_status, new_status, changed_by_id, note)
VALUES (
  (SELECT id FROM work_orders WHERE code = 'WO-2026-000001'),
  NULL, 'NEW',
  (SELECT id FROM users WHERE email = 'dispatcher@example.com'),
  'Work order raised from customer request'
);
