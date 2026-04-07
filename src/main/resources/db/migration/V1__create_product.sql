CREATE TABLE tb_product(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL UNIQUE,
    cost DECIMAL(10, 2) NOT NULL,
    price DECIMAL (10, 2) NOT NULL,
    current_stock INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);