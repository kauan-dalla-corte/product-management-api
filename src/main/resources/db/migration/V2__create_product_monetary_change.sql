CREATE TABLE tb_product_monetary_change(
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    new_value DECIMAL(10, 2) NOT NULL,
    old_value DECIMAL(10, 2) NOT NULL,
    monetary_field ENUM('PRICE', 'COST') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES tb_product (id)
);
