--liquibase formatted sql

--changeset aleph:create_advertisement_task_status_table
CREATE TABLE IF NOT EXISTS advertisement_task_status
(
    id varchar(36) PRIMARY KEY,
    status varchar(20) NOT NULL,
    error_message text,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_advertisement_task_status_status ON advertisement_task_status(status);
CREATE INDEX IF NOT EXISTS idx_advertisement_task_status_created_at ON advertisement_task_status(created_at);
