CREATE TABLE IF NOT EXISTS sales_order (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    product_code VARCHAR(30) NOT NULL,
    ordered_qty INT NOT NULL,
    produced_qty INT NOT NULL DEFAULT 0,
    defective_qty INT NOT NULL DEFAULT 0,
    shipped_qty INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(14,0) NOT NULL DEFAULT 0,
    total_amount DECIMAL(16,0) NOT NULL DEFAULT 0,
    order_date DATE NOT NULL,
    due_date DATE NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    memo VARCHAR(500), created_by VARCHAR(50), updated_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sales_order_customer FOREIGN KEY(customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_sales_order_product FOREIGN KEY(product_code) REFERENCES product(product_code)
);
CREATE INDEX idx_sales_order_due_status ON sales_order(due_date,order_status);
