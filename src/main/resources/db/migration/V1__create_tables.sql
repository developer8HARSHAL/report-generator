
CREATE TABLE report_jobs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id        VARCHAR(36)  NOT NULL UNIQUE,
    report_type   VARCHAR(50)  NOT NULL,
    status        ENUM('QUEUED','PROCESSING','DONE','FAILED','EXPIRED') DEFAULT 'QUEUED',
    requested_by  VARCHAR(100),
    parameters    JSON,
    file_path     VARCHAR(255),
    file_name     VARCHAR(100),
    error_msg     TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at  TIMESTAMP NULL,
    expires_at    TIMESTAMP NULL
);

CREATE TABLE sales_data(
    id BIGINT AUTO_INCREMENT UNIQUE KEY,
    product VARCHAR(100),
    region VARCHAR(50),
    amount DECIMAL(10,2),
    sold_at DATE
);

INSERT INTO sales_data(product,region,amount,sold_at) VALUES
('Laptop',   'North', 75000.00, '2024-01-15'),
('Phone',    'South', 25000.00, '2024-01-20'),
('Tablet',   'East',  35000.00, '2024-02-10'),
('Laptop',   'West',  80000.00, '2024-02-14'),
('Phone',    'North', 22000.00, '2024-03-05'),
('Headphone','South', 5000.00,  '2024-03-18'),
('Tablet',   'East',  33000.00, '2024-04-01'),
('Laptop',   'North', 90000.00, '2024-04-22'),
('Phone',    'West',  27000.00, '2024-05-10'),
('Headphone','North', 4500.00,  '2024-05-30');