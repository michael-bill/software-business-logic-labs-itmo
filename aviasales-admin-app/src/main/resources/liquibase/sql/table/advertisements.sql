--liquibase formatted sql

--changeset michael-bill:advertisements_table
create table if not exists advertisements
(
    id bigserial primary key,
    ad_type_id bigint not null references ad_types(id),
    title varchar(255) not null,
    company_name varchar(255),
    description text not null,
    deadline timestamp,
    created_at timestamp not null,
    created_by bigint references users(id)
);

create table if not exists advertisement_user_segments
(
    advertisement_id bigint not null references advertisements(id) on delete cascade,
    user_segment_id bigint not null references user_segments(id) on delete cascade,
    primary key (advertisement_id, user_segment_id)
);

create index idx_advertisements_ad_type_id on advertisements(ad_type_id);
create index idx_advertisements_created_by on advertisements(created_by);

