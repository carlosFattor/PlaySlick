# --- !Ups

CREATE TABLE orders (
    id uuid primary key default uuid_generate_v4(),
    ticket_block_id uuid,
    customer_name VARCHAR,
    customer_email VARCHAR,
    ticket_quantity INTEGER,
    timestamp timestamp with time zone,
    FOREIGN KEY (ticket_block_id) REFERENCES ticket_blocks(id)
);

# --- !Downs

DROP TABLE IF EXISTS orders;