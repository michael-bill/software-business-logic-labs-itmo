--liquibase formatted sql

--changeset michael-bill:sales_categories_table
create table if not exists sales_categories
(
    id bigserial primary key,
    name varchar(255) not null unique,
    description text not null,
    default_commission_percent numeric(5,2) not null,
    created_at timestamp not null,
    updated_at timestamp,
    created_by bigint references users(id),
    updated_by bigint references users(id)
);

create index idx_sales_categories_name on sales_categories(name);
create index idx_sales_categories_created_by on sales_categories(created_by);
create index idx_sales_categories_updated_by on sales_categories(updated_by);
