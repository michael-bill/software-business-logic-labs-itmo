--liquibase formatted sql

--changeset michael-bill:add_user_mock_data

insert into users (username, password, role) values
('admin', '$2a$10$RPNOc9j0WOphyWlKxqzs1ukBSm2yI0WszzqCMv4oHwvFE0hjrtEQm', 'ADMIN');

--changeset michael-bill:add_ad_mock_data

insert into ad_types (name, supports_segmentation, active) values
('Баннер на сайте', true, true),
('Аост в telegram', false, true),
('Реклама в рассылке', true, false),
('Видеореклама', true, true),
('Спецпроект', false, true),
('Ттатья', false, true),
('Сервис "короче"', false, true);

insert into user_segments (name, description, estimated_amount) values
('Частые путешественники', 'Пользователи, совершающие более 5 поездок в год', 15000),
('Любители бюджетных путешествий', 'Пользователи, ищущие билеты стоимостью до 20000 рублей', 50000),
('Путешественники бизнес-класса', 'Пользователи, предпочитающие перелеты бизнес-классом', 5000),
('Семья с детьми', 'Пользователи, путешествующие с детьми', 25000);

insert into advertisements (ad_type_id, title, company_name, description, deadline, created_at, created_by) values
(1, 'Специальное предложение на авиабилеты!', 'Авиакомпания "skyfly"', 'Лучшие цены на перелеты по всему миру!', '2024-07-30 23:59:59', now(), 1),
(2, 'Подпишись на наш telegram канал!', 'авиасейлс', 'эксклюзивные скидки и акции!', null, now(), 1),
(1, 'Путешествуйте бизнес-классом с комфортом', 'Авиакомпания "luxair"', 'Премиум сервис на борту!', '2024-08-15 23:59:59', now(), 1),
(4, 'Удивительное путешествие в тайланд', 'Туристическое агентство "traveldream"', 'Незабываемый отдых на райских островах', '2024-09-10 23:59:59', now(), 1);

insert into advertisement_user_segments (advertisement_id, user_segment_id) values
(1, 1),
(1, 2),
(3, 3),
(4, 1),
(4, 4);

--changeset michael-bill:insert_sales_test_data

-- Категории продаж
insert into sales_categories (name, description, default_commission_percent, created_at, created_by) values
('Авиабилеты по РФ', 'Продажа авиабилетов внутри России', 3.50, now(), 1),
('Авиабилеты за рубеж', 'Международные авиаперелеты', 4.00, now(), 1),
('Отели', 'Бронирование отелей', 7.00, now(), 1),
('Апартаменты', 'Аренда апартаментов и квартир', 8.50, now(), 1),
('Экскурсии', 'Организованные экскурсии', 10.00, now(), 1);

-- Единицы продаж (с привязкой к категориям)
insert into sales_units (name, description, sales_category_id, custom_commission_percent, is_custom_commission, created_at, created_by) values
('Trip.com', 'Международный сервис бронирования', 2, null, false, now(), 1),
('One Two Trip', 'Российский сервис бронирования', 1, null, false, now(), 1),
('Ostrovok.ru', 'Бронирование отелей в России', 3, 6.50, true, now(), 1),
('Booking.com', 'Международный сервис бронирования', 3, 5.00, true, now(), 1),
('Sutochno.ru', 'Аренда квартир посуточно', 4, null, false, now(), 1),
('Airbnb', 'Аренда жилья по всему миру', 4, 7.00, true, now(), 1),
('Sputnik8.com', 'Экскурсии по всему миру', 5, null, false, now(), 1),
('Tutu.ru', 'Российский сервис бронирования', 1, 2.50, true, now(), 1),
('Kiwi.com', 'Международный сервис бронирования', 2, 3.25, true, now(), 1);
