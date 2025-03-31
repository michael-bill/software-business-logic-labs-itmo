--liquibase formatted sql

--changeset michael-bill:advertisements_user_segments_table
create table if not exists advertisement_user_segments
(
    advertisement_id bigint not null references advertisements(id) on delete cascade,
    user_segment_id bigint not null references user_segments(id) on delete cascade,
    primary key (advertisement_id, user_segment_id)
);

create index idx_advertisement_user_segments_advertisement_id on advertisement_user_segments(advertisement_id);
create index idx_advertisement_user_segments_user_segment_id on advertisement_user_segments(user_segment_id);
