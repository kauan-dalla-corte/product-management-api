CREATE TABLE tb_product_stock_movement(
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    type ENUM('INBOUND', 'OUTBOUND') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES tb_product (id)
);