CREATE TABLE credentials (
    user_id BIGINT PRIMARY KEY,
    login VARCHAR(63) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL
);