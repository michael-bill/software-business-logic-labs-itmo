--liquibase formatted sql

--changeset michael-bill:users_table
CREATE TABLE IF NOT EXISTS users
(
    id bigserial PRIMARY KEY,
    username varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    role varchar(32) NOT NULL
);

CREATE INDEX idx_username ON users(username);
