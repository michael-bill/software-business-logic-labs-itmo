--liquibase formatted sql

--changeset michael-bill:user_segments_table
CREATE TABLE IF NOT EXISTS user_segments
(
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    description text NOT NULL,
    estimated_amount integer
);

create index if not exists idx_user_segments_name ON user_segments(name);
