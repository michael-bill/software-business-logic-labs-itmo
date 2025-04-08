--liquibase formatted sql

--changeset michael-bill:sales_units_table
CREATE TABLE IF NOT EXISTS sales_units
(
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    description text NOT NULL,
    sales_category_id bigint NOT NULL REFERENCES sales_categories(id),
    custom_commission_percent numeric(5,2),
    is_custom_commission boolean NOT NULL DEFAULT false,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    created_by bigint REFERENCES users(id),
    updated_by bigint REFERENCES users(id)
);

CREATE INDEX idx_sales_units_name ON sales_units(name);
CREATE INDEX idx_sales_units_sales_category_id ON sales_units(sales_category_id);
CREATE INDEX idx_sales_units_created_by ON sales_units(created_by);
CREATE INDEX idx_sales_units_updated_by ON sales_units(updated_by);

--changeset michael-bill:add_version_colum_to_sales_categories_table
alter table sales_units add column version bigint not null default 0;
