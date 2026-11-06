-- 다온테크 ERP 재고/거래처 확장 (최초 1회 실행)
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    manager_name VARCHAR(50),
    phone VARCHAR(30),
    email VARCHAR(100),
    address VARCHAR(255),
    memo VARCHAR(500),
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE product_stock_history
    MODIFY COLUMN type VARCHAR(20) NOT NULL,
    ADD COLUMN balance_after INT NOT NULL DEFAULT 0 AFTER quantity,
    ADD COLUMN customer_id BIGINT NULL AFTER balance_after;

INSERT IGNORE INTO product_stock(product_code, quantity)
SELECT product_code, 0 FROM product;

-- 이미지 원본의 명칭 오타 보정
UPDATE product SET color = '팁그레이' WHERE product_code = 'CB-DGR';
UPDATE product SET product_name = '토마토샐러드' WHERE product_code = 'SET-TMT';

-- 2025년 8월 엑셀 마감재고. OPENING 이력이 없을 때만 이관한다.
UPDATE product_stock s
JOIN (
    SELECT 'CB-PNK' code, 2090 qty UNION ALL
    SELECT 'CB-DGR', 2045 UNION ALL SELECT 'CB-CGR', 2731 UNION ALL
    SELECT 'CB-COA', 1896 UNION ALL SELECT 'CB-CRM', 2125 UNION ALL
    SELECT 'CB-YEL', 171 UNION ALL SELECT 'CB-SBL', 1055 UNION ALL
    SELECT 'CB-LAV', 158 UNION ALL SELECT 'CB-SML', 2080 UNION ALL
    SELECT 'CB-CAR', 1476 UNION ALL SELECT 'CB-OGR', 1289 UNION ALL
    SELECT 'CB-BEO', 1682 UNION ALL SELECT 'CB-NAV', 874
) x ON x.code = s.product_code
SET s.quantity = x.qty
WHERE NOT EXISTS (SELECT 1 FROM product_stock_history WHERE type = 'OPENING');

INSERT INTO product_stock_history(product_code, type, quantity, balance_after, memo, created_at)
SELECT x.code, 'OPENING', x.qty, x.qty, '기존 엑셀 2025년 8월 마감재고 이관', '2025-08-31 23:59:59'
FROM (
    SELECT 'CB-PNK' code, 2090 qty UNION ALL
    SELECT 'CB-DGR', 2045 UNION ALL SELECT 'CB-CGR', 2731 UNION ALL
    SELECT 'CB-COA', 1896 UNION ALL SELECT 'CB-CRM', 2125 UNION ALL
    SELECT 'CB-YEL', 171 UNION ALL SELECT 'CB-SBL', 1055 UNION ALL
    SELECT 'CB-LAV', 158 UNION ALL SELECT 'CB-SML', 2080 UNION ALL
    SELECT 'CB-CAR', 1476 UNION ALL SELECT 'CB-OGR', 1289 UNION ALL
    SELECT 'CB-BEO', 1682 UNION ALL SELECT 'CB-NAV', 874
) x
WHERE NOT EXISTS (SELECT 1 FROM product_stock_history WHERE type = 'OPENING');
