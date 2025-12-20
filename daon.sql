ALTER TABLE product
ADD COLUMN price INT NOT NULL COMMENT '단가';

ALTER TABLE product
MODIFY created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
MODIFY updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP;

    
    ALTER TABLE product
ADD created_by VARCHAR(50),
ADD updated_by VARCHAR(50);



SELECT
  product_code,
  created_at,
  updated_at
FROM product
ORDER BY product_code;



ALTER TABLE product
MODIFY price INT NOT NULL DEFAULT 0;

ALTER TABLE product
MODIFY created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
MODIFY updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP;

    
    INSERT INTO product
(product_code, product_name, type, color, category, use_yn, price)
VALUES
-- ① 도마 (CB)
('CB-PNK','도마','SINGLE','핑크','도마','Y',0),
('CB-DGR','도마','SINGLE','딥그레이','도마','Y',0),
('CB-CGR','도마','SINGLE','쿨그레이','도마','Y',0),
('CB-COA','도마','SINGLE','코코아','도마','Y',0),
('CB-CRM','도마','SINGLE','크림화이트','도마','Y',0),
('CB-YEL','도마','SINGLE','옐로우','도마','Y',0),
('CB-SBL','도마','SINGLE','소프트블루','도마','Y',0),
('CB-LAV','도마','SINGLE','라벤더','도마','Y',0),
('CB-SML','도마','SINGLE','소프트멜론','도마','Y',0),
('CB-CAR','도마','SINGLE','카라멜','도마','Y',0),
('CB-OGR','도마','SINGLE','우드그린','도마','Y',0),
('CB-BEO','도마','SINGLE','번트오렌지','도마','Y',0),
('CB-NAV','도마','SINGLE','네이비','도마','Y',0),

-- ② 거치대 (ST)
('ST-IVR','거치대','SINGLE','아이보리','거치대','Y',0),
('ST-BYL','거치대','SINGLE','버터옐로우','거치대','Y',0),
('ST-SML','거치대','SINGLE','소프트멜론','거치대','Y',0),
('ST-SBL','거치대','SINGLE','소프트블루','거치대','Y',0),
('ST-BLK','거치대','SINGLE','블랙','거치대','Y',0),

-- ③ 세트 (SET) → color 없음 = NULL
('SET-BEST','베스트세트','SET',NULL,'세트','Y',0),
('SET-BLK','블랙에디션','SET',NULL,'세트','Y',0),
('SET-TMT','토마토 갤러드','SET',NULL,'세트','Y',0),
('SET-VVD','비비드','SET',NULL,'세트','Y',0),
('SET-SUN','따스한 햇살','SET',NULL,'세트','Y',0),
('SET-YMJ','야무진','SET',NULL,'세트','Y',0);


SELECT
  product_code,
  product_name,
  type,
  color,
  category,
  price,
  use_yn,
  created_at,
  updated_at
FROM product
ORDER BY product_code;

CREATE TABLE product_stock (
    product_code VARCHAR(20) PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 0
);

CREATE TABLE product_stock_history (
    stock_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL,
    type ENUM('IN','OUT') NOT NULL,
    quantity INT NOT NULL,
    memo VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);





