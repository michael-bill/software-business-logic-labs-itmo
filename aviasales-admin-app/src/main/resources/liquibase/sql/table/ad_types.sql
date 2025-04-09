--liquibase formatted sql

--changeset michael-bill:ad_types_table
CREATE TABLE IF NOT EXISTS ad_types
(
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    supports_segmentation boolean NOT NULL,
    active boolean NOT NULL
);

create index if not exists idx_ad_types_name ON ad_types(name);
