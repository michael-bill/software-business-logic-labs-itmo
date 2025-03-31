--liquibase formatted sql

--changeset michael-bill:users_table
create table if not exists users
(
    id bigserial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null,
    role varchar(32) not null
);

create index idx_username on users(username);
