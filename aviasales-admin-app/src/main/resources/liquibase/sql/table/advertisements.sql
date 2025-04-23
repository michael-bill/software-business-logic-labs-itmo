--liquibase formatted sql

--changeset michael-bill:advertisements_table
CREATE TABLE IF NOT EXISTS advertisements
(
    id bigserial PRIMARY KEY,
    ad_type_id bigint NOT NULL REFERENCES ad_types(id),
    title varchar(255) NOT NULL,
    company_name varchar(255),
    description text NOT NULL,
    deadline timestamp,
    created_at timestamp NOT NULL,
    created_by bigint REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS advertisement_user_segments
(
    advertisement_id bigint NOT NULL REFERENCES advertisements(id) ON DELETE CASCADE,
    user_segment_id bigint NOT NULL REFERENCES user_segments(id) ON DELETE CASCADE,
    PRIMARY KEY (advertisement_id, user_segment_id)
);

create index if not exists idx_advertisements_ad_type_id ON advertisements(ad_type_id);
create index if not exists idx_advertisements_created_by ON advertisements(created_by);

--changeset michael-bill:add_payment_fields_to_advertisements
ALTER TABLE advertisements ADD COLUMN IF NOT EXISTS payed boolean NOT NULL DEFAULT false;
ALTER TABLE advertisements ADD COLUMN IF NOT EXISTS invoice_id varchar(50);
ALTER TABLE advertisements ADD COLUMN IF NOT EXISTS payment_page text;
ALTER TABLE advertisements ADD COLUMN IF NOT EXISTS payment_initiated_at timestamp;
ALTER TABLE advertisements ADD COLUMN IF NOT EXISTS payment_expires_at timestamp;

CREATE INDEX IF NOT EXISTS idx_advertisements_payed ON advertisements(payed);
CREATE INDEX IF NOT EXISTS idx_advertisements_invoice_id ON advertisements(invoice_id);
