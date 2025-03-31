--liquibase formatted sql

--changeset michael-bill:sales_units_table
create table if not exists sales_units
(
    id bigserial primary key,
    name varchar(255) not null unique,
    description text not null,
    sales_category_id bigint not null references sales_categories(id),
    custom_commission_percent numeric(5,2),
    is_custom_commission boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp,
    created_by bigint references users(id),
    updated_by bigint references users(id)
);

create index idx_sales_units_name on sales_units(name);
create index idx_sales_units_sales_category_id on sales_units(sales_category_id);
create index idx_sales_units_created_by on sales_units(created_by);
create index idx_sales_units_updated_by on sales_units(updated_by);
