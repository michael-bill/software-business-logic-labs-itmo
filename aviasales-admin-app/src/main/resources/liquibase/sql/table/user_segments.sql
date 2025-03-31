--liquibase formatted sql

--changeset michael-bill:user_segments_table
create table if not exists user_segments
(
    id bigserial primary key,
    name varchar(255) not null unique,
    description text not null,
    estimated_amount integer
);

create index idx_user_segments_name on user_segments(name);
