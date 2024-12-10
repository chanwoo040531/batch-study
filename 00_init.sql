CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator_password';
SELECT pg_create_physical_replication_slot('replication_slot');

-- CREATE SCHEMA IF NOT EXISTS coupon;
--
-- CREATE TABLE IF NOT EXISTS coupon.coupons
-- (
--     id         BIGSERIAL PRIMARY KEY,
--     code       VARCHAR(255)   NOT NULL,
--     discount   DECIMAL(10, 2) NOT NULL,
--     state      VARCHAR(255)   NOT NULL DEFAULT 'AVAILABLE',
--     expired_at TIMESTAMP      NOT NULL
-- );
--
-- INSERT INTO coupon.coupons (code, discount, expired_at)
-- SELECT 'some-code' || i,
--        10.00,
--        generated_expired_at
-- FROM generate_series(1, 255) as i,
--      generate_series(
--              TIMESTAMP '2020-12-31 23:59:59',
--              TIMESTAMP '2025-12-31 23:59:59',
--              INTERVAL '1 day'
--      ) AS generated_expired_at
-- ;

CREATE SCHEMA IF NOT EXISTS account;

CREATE TABLE IF NOT EXISTS account.customers (
    id     BIGSERIAL PRIMARY KEY,
    age    INTEGER,
    gender VARCHAR(255),
    name   VARCHAR(255)
);

INSERT INTO account.customers (age, gender, name)
VALUES (25, 'M', 'James Smith'),
    (32, 'F', 'Mary Johnson'),
    (45, 'M', 'Robert Brown'),
    (28, 'F', 'Linda Davis'),
    (36, 'M', 'Michael Wilson'),
    (41, 'F', 'Elizabeth Moore'),
    (29, 'M', 'William Taylor'),
    (34, 'F', 'Jennifer Anderson'),
    (48, 'M', 'David Thomas'),
    (23, 'F', 'Jessica Martinez'),
    (50, 'M', 'Richard Garcia'),
    (27, 'F', 'Sarah Harris'),
    (39, 'M', 'Thomas Clark'),
    (31, 'F', 'Karen Lewis'),
    (22, 'M', 'Christopher Walker'),
    (30, 'F', 'Emily Hall'),
    (46, 'M', 'Andrew Young'),
    (38, 'F', 'Michelle King'),
    (26, 'M', 'Brian Allen'),
    (35, 'F', 'Amanda Scott')

