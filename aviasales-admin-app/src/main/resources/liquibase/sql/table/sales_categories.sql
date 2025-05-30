--liquibase formatted sql

--changeset michael-bill:sales_categories_table
CREATE TABLE IF NOT EXISTS sales_categories
(
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    description text NOT NULL,
    default_commission_percent numeric(5,2) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    created_by bigint REFERENCES users(id),
    updated_by bigint REFERENCES users(id)
);

create index if not exists idx_sales_categories_name ON sales_categories(name);
create index if not exists idx_sales_categories_created_by ON sales_categories(created_by);
create index if not exists idx_sales_categories_updated_by ON sales_categories(updated_by);

--changeset michael-bill:add_version_colum_to_sales_categories_table
alter table sales_categories add column version bigint not null default 0;
