CREATE TABLE IF NOT EXISTS employee (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    employee_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    position_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(150),
    hire_date DATE,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    hourly_wage DECIMAL(14,2) NOT NULL DEFAULT 0,
    weekly_scheduled_hours DECIMAL(6,2) NOT NULL DEFAULT 40,
    weekly_scheduled_days INT NOT NULL DEFAULT 5,
    daily_scheduled_hours DECIMAL(6,2) NOT NULL DEFAULT 8,
    annual_leave_total DECIMAL(5,1) NOT NULL DEFAULT 15.0,
    resume_original_name VARCHAR(255),
    resume_stored_name VARCHAR(255),
    memo TEXT,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee_leave_history (
    leave_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_date DATE NOT NULL,
    days DECIMAL(4,1) NOT NULL,
    leave_type VARCHAR(30) NOT NULL DEFAULT 'ANNUAL',
    memo VARCHAR(500),
    created_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_leave_employee FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
);

CREATE INDEX idx_employee_status ON employee(employment_status);
CREATE INDEX idx_employee_leave_date ON employee_leave_history(employee_id, leave_date);

CREATE TABLE IF NOT EXISTS employee_payroll (
    payroll_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    pay_month CHAR(7) NOT NULL,
    base_salary DECIMAL(14,0) NOT NULL DEFAULT 0,
    allowance DECIMAL(14,0) NOT NULL DEFAULT 0,
    extra_allowance DECIMAL(14,0) NOT NULL DEFAULT 0,
    deduction DECIMAL(14,0) NOT NULL DEFAULT 0,
    net_salary DECIMAL(14,0) NOT NULL DEFAULT 0,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    memo VARCHAR(500),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_employee_pay_month(employee_id, pay_month),
    CONSTRAINT fk_employee_payroll_employee FOREIGN KEY(employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE IF NOT EXISTS employee_work_allowance (
    work_allowance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    board_quantity INT NOT NULL DEFAULT 0,
    board_unit_price DECIMAL(14,2) NOT NULL DEFAULT 0,
    board_allowance DECIMAL(14,0) NOT NULL DEFAULT 0,
    overtime_hours DECIMAL(6,2) NOT NULL DEFAULT 0,
    hourly_wage DECIMAL(14,2) NOT NULL DEFAULT 0,
    overtime_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.50,
    overtime_allowance DECIMAL(14,0) NOT NULL DEFAULT 0,
    total_allowance DECIMAL(14,0) NOT NULL DEFAULT 0,
    attended TINYINT(1) NOT NULL DEFAULT 1,
    memo VARCHAR(500), created_by VARCHAR(50), updated_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_employee_work_date(employee_id, work_date),
    CONSTRAINT fk_work_allowance_employee FOREIGN KEY(employee_id) REFERENCES employee(employee_id)
);
