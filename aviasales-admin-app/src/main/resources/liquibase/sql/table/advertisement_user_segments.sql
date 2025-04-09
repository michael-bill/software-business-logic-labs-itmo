--liquibase formatted sql

--changeset michael-bill:advertisements_user_segments_table
CREATE TABLE IF NOT EXISTS advertisement_user_segments
(
    advertisement_id bigint NOT NULL REFERENCES advertisements(id) ON DELETE CASCADE,
    user_segment_id bigint NOT NULL REFERENCES user_segments(id) ON DELETE CASCADE,
    PRIMARY KEY (advertisement_id, user_segment_id)
);

create index if not exists idx_advertisement_user_segments_advertisement_id ON advertisement_user_segments(advertisement_id);
create index if not exists idx_advertisement_user_segments_user_segment_id ON advertisement_user_segments(user_segment_id);
