CREATE TABLE products (
    product_id BIGINT PRIMARY KEY,
    product_sku BIGINT NOT NULL,
    product_name VARCHAR(50),
    product_amount BIGINT,
    product_data VARCHAR(120)
);


CREATE TABLE loyality_data (
    product_sku BIGINT PRIMARY KEY,
    loyality_data VARCHAR(120)
);
