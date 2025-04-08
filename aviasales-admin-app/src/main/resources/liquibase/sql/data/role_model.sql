--liquibase formatted sql

-- был бы у нас хороший вариант, а не с ролевой моделью в xml, то тут бы было что-то красивое...

--changeset michael-bill:bye_bye_users...
alter table sales_categories drop column created_by;
alter table sales_categories drop column updated_by;

alter table sales_units drop column created_by;
alter table sales_units drop column updated_by;

alter table advertisements drop column created_by;

drop table if exists users;
