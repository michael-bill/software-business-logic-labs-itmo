--liquibase formatted sql

--changeset michael-bill:ad_types_table
create table if not exists ad_types
(
    id bigserial primary key,
    name varchar(255) not null unique,
    supports_segmentation boolean not null,
    active boolean not null
);

create index idx_ad_types_name on ad_types(name);
