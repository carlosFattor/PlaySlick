# --- !Ups

CREATE TABLE events (
    id uuid primary key default uuid_generate_v4(),
    name VARCHAR,
    event_start timestamp with time zone default (now() at time zone 'BRST'),
    event_end timestamp with time zone,
    address VARCHAR,
    city VARCHAR,
    state VARCHAR,
    country CHAR(2)
);

CREATE TABLE ticket_blocks (
    id uuid primary key default uuid_generate_v4(),
    event_id uuid,
    name VARCHAR,
    product_code VARCHAR(40),
    price DECIMAL,
    initial_size INTEGER,
    sale_start timestamp with time zone default (now() at time zone 'BRST'),
    sale_end timestamp with time zone,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

# --- !Downs

DROP TABLE IF EXISTS ticket_blocks;
DROP TABLE IF EXISTS events;