CREATE SCHEMA IF NOT EXISTS coupon;

CREATE TABLE IF NOT EXISTS coupon.coupons (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(255)   NOT NULL,
    discount   DECIMAL(10, 2) NOT NULL,
    state      VARCHAR(255)   NOT NULL DEFAULT 'AVAILABLE',
    expired_at TIMESTAMP      NOT NULL
);

INSERT INTO coupon.coupons (code, discount, expired_at)
SELECT 'some-code' || i,
    10.00,
    generated_expired_at
FROM
    generate_series(1, 255) as i,
    generate_series(
        TIMESTAMP '2020-12-31 23:59:59',
        TIMESTAMP '2025-12-31 23:59:59',
        INTERVAL '1 day'
     ) AS generated_expired_at
;