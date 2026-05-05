-- Create product table
CREATE TABLE product (
    id        BIGSERIAL    PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

-- Join table linking orders to products (many-to-many)
CREATE TABLE order_product (
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_op_order   FOREIGN KEY (order_id)   REFERENCES "order"(id) ON DELETE CASCADE,
    CONSTRAINT fk_op_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Performance: index on order.customer_id speeds up JOIN between order and customer
CREATE INDEX idx_order_customer_id ON "order"(customer_id);

-- Performance: index on customer.name speeds up ILIKE substring searches
CREATE INDEX idx_customer_name ON customer(name);

-- Performance: index on the product_id side of the join table for reverse lookups
CREATE INDEX idx_order_product_product_id ON order_product(product_id);

