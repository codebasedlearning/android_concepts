-- (C) 2025 A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

-- you can run this script from supabase sql console, or from
-- the python script as plain text with 'cur.execute(text)'

-- -- ami_zone -- --


-- Be careful here, because if you delete the schema,
-- all the data it contains will also be deleted.

-- drop schema ami_zone CASCADE;

create schema if not exists ami_zone;

-- -- div -- --

-- location
create table if not exists ami_zone.div_location
(
    id           int          not null,
    place        varchar(100) not null,
    abbreviation varchar(20)  not null,
    primary key (id)
);
insert into ami_zone.div_location (id, place, abbreviation)
values  (1, 'Aachen', 'AC'),
        (2, 'Jülich', 'JÜL'),
        (3, 'Köln', 'K'),
        (4, 'Berlin', 'B');
-- select if(count(id)=4 and max(id)=4,'ok','nok') from ami_zone.div_location;

-- department
create table if not exists ami_zone.div_department
(
    id           int          not null,
    name         varchar(100) not null,
    abbreviation varchar(20)  not null,
    head         varchar(100) not null,
    primary key (id)
);
insert into ami_zone.div_department (id, name, abbreviation, head)
values  (1, 'Board', 'BR', 'BR'),
        (2, 'Human Resources', 'HR', 'CHRO'),
        (3, 'Marketing and Sales', 'MS', 'CMO'),
        (4, 'Finance', 'FN', 'CFO'),
        (5, 'Research and Development', 'RD', 'CTO'),
        (6, 'Project Management', 'PM', 'COO'),
        (7, 'Vehicle Fleet', 'VF', 'COO');
-- select if(count(id)=7 and max(id)=7,'ok','nok') from ami_zone.div_department;

-- room
create table if not exists ami_zone.div_room
(
    location_id int           not null,
    room        varchar(100)  not null,
    seats       int default 1 not null,
    primary key (location_id, room),
    constraint div_room_div_location_id_fk
        foreign key (location_id) references ami_zone.div_location (id)
            on update cascade on delete cascade
);
insert into ami_zone.div_room (location_id, room, seats)
values  (3, '102.1', 2),
        (3, '103.1', 2),
        (4, '102.1', 1),
        (4, '103.1', 1);
-- select if(count(location_id)=4 and max(location_id)=4,'ok','nok') from ami_zone.div_room;

-- located_in
create table if not exists ami_zone.div_located_in
(
    id             int               not null,
    department_id  int               null,
    location_id    int               null,
    is_headquarter int default 0 not null,
    primary key (id),
    constraint located_in_department_id_fk
        foreign key (department_id) references ami_zone.div_department (id)
            on update cascade on delete set null,
    constraint located_in_location_id_fk
        foreign key (location_id) references ami_zone.div_location (id)
            on update cascade on delete set null
);
insert into ami_zone.div_located_in (id, department_id, location_id, is_headquarter)
values  (1, 1, 4, 1),
        (2, 2, 4, 1),
        (3, 3, 1, 0),
        (4, 3, 2, 0),
        (5, 3, 3, 0),
        (6, 3, 4, 1),
        (7, 4, 4, 1),
        (8, 5, 1, 1),
        (9, 5, 2, 0),
        (10, 6, 3, 0),
        (11, 6, 4, 1);
-- select if(count(id)=11 and max(id)=11,'ok','nok') from ami_zone.div_located_in;


-- -- hr -- --

-- task
create table if not exists ami_zone.hr_task
(
    id   int          not null,
    name varchar(100) not null,
    primary key (id)
);
insert into ami_zone.hr_task (id, name)
values  (1, 'Regular tasks'),
        (2, 'Education'),
        (3, 'Project Alpha'),
        (4, 'Project Zerberus'),
        (5, 'Project XCoin');
-- select if(count(id)=5 and max(id)=5,'ok','nok') from ami_zone.hr_task;

-- team
create table if not exists ami_zone.hr_team
(
    id           int          not null,
    name         varchar(100) not null,
    abbreviation varchar(20)  not null,
    primary key (id)
);
insert into ami_zone.hr_team (id, name, abbreviation)
values  (1, 'Board', 'BR'),
        (2, 'Management', 'MN'),
        (3, 'Employee', 'EM'),
        (4, 'Apprentice', 'AP'),
        (5, 'Student', 'ST'),
        (6, 'Freelancer', 'FL');
-- select if(count(id)=6 and max(id)=6,'ok','nok') from ami_zone.hr_team;

-- employee, note: there are limits in 'on update' and 'on delete'
create table if not exists ami_zone.hr_employee
(
    id          int            not null,
    name        varchar(100)   not null,
    salary      decimal(12, 2) not null,
    employee_id int            null, -- comment 'reports to',
    comment     varchar(100)   null,
    primary key (id),
    constraint hr_employee_hr_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null
);
insert into ami_zone.hr_employee (id, name, salary, employee_id, comment)
values  (1, 'Board', 0.00, 1, 'Board (artificial)'),
        (2, 'Paul', 5000.00, 1, 'Board'),
        (3, 'Hannah', 5000.00, 1, 'Board'),
        (4, 'Luka', 5000.00, 1, 'Board'),
        (5, 'Mia', 110000.00, 1, 'Board, CEO'),
        (6, 'Ben', 90000.00, 5, 'CTO, COO'),
        (7, 'Emma', 90000.00, 5, 'CHRO, CFO'),
        (8, 'Sofia', 90000.00, 5, 'CMO, Sales'),
        (9, 'Jonas', 50000.00, 5, 'Assist - Team Mia'),
        (10, 'Anna', 50000.00, 6, 'IT - Team Ben'),
        (11, 'Finn', 50000.00, 6, 'Expert - Team Ben'),
        (12, 'Lea', 50000.00, 6, 'PM - Team Ben'),
        (13, 'Leon', 70000.00, 6, 'PM - Team Ben'),
        (14, 'Emilia', 70000.00, 7, 'Controller Team Emma'),
        (15, 'Luis', 50000.00, 12, 'Dev - Team Lea'),
        (16, 'Marie', 50000.00, 12, 'Dev - Team Lea'),
        (17, 'Max', 12000.00, 12, 'Dev - Team Lea - Student'),
        (18, 'Lukas', 50000.00, 13, 'Dev - Team Leon'),
        (19, 'Lena', 50000.00, 13, 'Dev - Team Leon'),
        (20, 'Leonie', 18000.00, 13, 'Dev - Team Leon - Apprentice'),
        (21, 'Lilly', 24000.00, 8, 'Marketing - Team Sophia '),
        (22, 'Felix', 60000.00, 8, 'Sales - Team Sophia'),
        (23, 'Tim', 60000.00, 8, 'Sales - Team Sophia');
-- select if(count(id)=23 and max(id)=23,'ok','nok') from ami_zone.hr_employee;

-- part_of
create table if not exists ami_zone.hr_part_of
(
    id          int not null,
    employee_id int null,
    team_id     int null,
    primary key (id),
    constraint hr_part_of_hr_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null,
    constraint hr_part_of_hr_team_id_fk
        foreign key (team_id) references ami_zone.hr_team (id)
            on update cascade on delete set null
);
insert into ami_zone.hr_part_of (id, employee_id, team_id)
values  (1, 1, 1),
        (2, 2, 1),
        (3, 2, 6),
        (4, 3, 1),
        (5, 3, 6),
        (6, 4, 1),
        (7, 4, 6),
        (8, 5, 1),
        (9, 5, 2),
        (10, 5, 3),
        (11, 6, 2),
        (12, 6, 3),
        (13, 7, 2),
        (14, 7, 3),
        (15, 8, 2),
        (16, 8, 3),
        (17, 9, 3),
        (18, 10, 3),
        (19, 11, 6),
        (20, 12, 3),
        (21, 13, 3),
        (22, 14, 3),
        (23, 15, 6),
        (24, 16, 3),
        (25, 17, 3),
        (26, 17, 5),
        (27, 18, 3),
        (28, 19, 3),
        (29, 20, 3),
        (30, 20, 4),
        (31, 21, 6),
        (32, 22, 3),
        (33, 23, 3);
-- select if(count(id)=33 and max(id)=33,'ok','nok') from ami_zone.hr_part_of;

-- cat
create table if not exists ami_zone.hr_cat
(
    id          int         not null,
    name        varchar(30) not null,
    employee_id int         null, -- comment 'owner',
    primary key (id),
    constraint cat_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null
);
insert into ami_zone.hr_cat (id, name, employee_id)
values  (1, 'Mauzi', 2),
        (2, 'Mini', 17),
        (3, 'Roy', null);
-- select if(count(id)=3 and max(id)=3,'ok','nok') from ami_zone.hr_cat;

-- works_in_at
create table if not exists ami_zone.hr_works_in_at
(
    id             int                         not null,
    employee_id    int                         null,
    department_id  int                         null,
    task_id        int                         null,
    hours_per_week decimal(6, 2) default 40.00 not null,
    primary key (id),
    constraint hr_works_in_at_div_department_id_fk
        foreign key (department_id) references ami_zone.div_department (id)
            on update cascade on delete set null,
    constraint hr_works_in_at_hr_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null,
    constraint hr_works_in_at_hr_task_id_fk
        foreign key (task_id) references ami_zone.hr_task (id)
            on update cascade on delete set null
);
insert into ami_zone.hr_works_in_at (id, employee_id, department_id, task_id, hours_per_week)
values  (1, 1, 1, 1, 0.00),
        (2, 2, 1, 1, 5.00),
        (3, 3, 1, 1, 5.00),
        (4, 4, 1, 1, 5.00),
        (5, 5, 1, 1, 30.00),
        (6, 5, 4, 5, 10.00),
        (7, 6, 5, 1, 20.00),
        (8, 6, 6, 1, 20.00),
        (9, 7, 2, 1, 20.00),
        (10, 7, 4, 1, 10.00),
        (11, 7, 4, 5, 10.00),
        (12, 8, 3, 1, 40.00),
        (13, 9, 1, 1, 40.00),
        (14, 10, 5, 1, 40.00),
        (15, 11, 5, 1, 30.00),
        (16, 11, 5, 3, 10.00),
        (17, 12, 6, 1, 40.00),
        (18, 13, 6, 1, 30.00),
        (19, 13, 6, 4, 10.00),
        (20, 14, 4, 1, 40.00),
        (21, 15, 5, 1, 40.00),
        (22, 16, 5, 1, 40.00),
        (23, 17, 5, 1, 20.00),
        (24, 17, 5, 2, 20.00),
        (25, 18, 5, 1, 40.00),
        (26, 19, 5, 1, 40.00),
        (27, 20, 5, 1, 20.00),
        (28, 20, 5, 2, 20.00),
        (29, 21, 3, 1, 30.00),
        (30, 21, 5, 4, 10.00),
        (31, 22, 3, 1, 40.00),
        (32, 23, 3, 1, 40.00);
-- select if(count(id)=32 and max(id)=32,'ok','nok') from ami_zone.hr_works_in_at;


-- -- assets -- --

-- catalogue
create table if not exists ami_zone.asset_catalogue
(
    id                int          not null,
    item              varchar(100) not null,
    manufacturer_code varchar(100) not null,
    primary key (id)
);
insert into ami_zone.asset_catalogue (id, item, manufacturer_code)
values  (1, 'Chair JÄRVFJÄLLET', '892.756.23'),
        (2, 'Chair HATTEFJÄLL', '892.521.36'),
        (3, 'Chair RENBERGET', '203.394.20'),
        (4, 'Table SKARSTA', '593.248.18'),
        (5, 'Table BEKANT', '490.064.06'),
        (6, 'Table IDÅSEN', '992.810.39'),
        (7, 'Mobile iPhone Pro', 'MWC22ZD/A'),
        (8, 'Mobile iPhone Max', 'MWHQ2ZD/A'),
        (9, 'Mobile Pixel', 'GA01188-DE');
-- select if(count(id)=9 and max(id)=9,'ok','nok') from ami_zone.asset_catalogue;

-- inventory
create table if not exists ami_zone.asset_inventory
(
    id           int                         not null,
    barcode      varchar(100)                not null,
    price        decimal(12, 2) default 0.00 not null,
    catalogue_id int                         null,
    primary key (id),
    constraint asset_inventory_asset_catalogue_id_fk
        foreign key (catalogue_id) references ami_zone.asset_catalogue (id)
            on update cascade on delete set null
);
insert into ami_zone.asset_inventory (id, barcode, price, catalogue_id)
values  (1, 'C-001', 193.00, 1),
        (2, 'C-002', 39.00, 3),
        (3, 'T-001', 262.00, 6),
        (4, 'T-002', 263.44, 5),
        (5, 'M-001', 1120.00, 7),
        (6, 'M-002', 1120.00, 7),
        (7, 'M-003', 551.00, 9);
-- select if(count(id)=7 and max(id)=7,'ok','nok') from ami_zone.asset_inventory;

-- inventory_furniture
create table if not exists ami_zone.asset_inventory_furniture
(
    id          int          not null,
    color       varchar(20)  null,
    location_id int          null,
    room        varchar(100) null,
    primary key (id),
    constraint asset_inventory_furniture_asset_inventory_id_fk
        foreign key (id) references ami_zone.asset_inventory (id)
            on update cascade on delete cascade,
    constraint asset_inventory_furniture_div_room_location_id_room_fk
        foreign key (location_id, room) references ami_zone.div_room (location_id, room)
            on update cascade on delete set null
);
insert into ami_zone.asset_inventory_furniture (id, color, location_id, room)
values  (1, 'brown', 3, '102.1'),
        (2, 'white', 3, '103.1'),
        (3, 'black', 4, '103.1'),
        (4, 'black', 4, '102.1');
-- select if(count(id)=7 and max(id)=7,'ok','nok') from ami_zone.asset_inventory_furniture;

-- inventory_mobile
create table if not exists ami_zone.asset_inventory_mobile
(
    id          int          not null,
    os          varchar(100) not null,
    employee_id int          null, -- comment 'owner',
    primary key (id),
    constraint asset_inventory_mobile_asset_inventory_id_fk
        foreign key (id) references ami_zone.asset_inventory (id)
            on update cascade on delete cascade,
    constraint asset_inventory_mobile_hr_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null
);
insert into ami_zone.asset_inventory_mobile (id, os, employee_id)
values  (5, 'iOS', 2),
        (6, 'iOS', 3),
        (7, 'Android', 4);
-- select if(count(id)=3 and max(id)=7,'ok','nok') from ami_zone.asset_inventory_mobile;


-- -- shop -- --

-- category
create table if not exists ami_zone.shop_category
(
    id   int          not null,
    name varchar(100) not null,
    primary key (id)
);
insert into ami_zone.shop_category (id, name)
values  (1, 'Frozen Goods'),
        (2, 'Fruits, Vegetables'),
        (3, 'Sausages, Cold Meat'),
        (4, 'Milk Products'),
        (5, 'Cold Drinks'),
        (6, 'Barbecue Products'),
        (7, 'Snacks'),
        (8, 'Hot Drinks'),
        (9, 'Convenience Foods'),
        (10, 'Baked Goods'),
        (11, 'Magazines'),
        (12, 'Services');
-- select if(count(id)=12 and max(id)=12,'ok','nok') from ami_zone.shop_category;

-- product
create table if not exists ami_zone.shop_product
(
    id          int                         not null,
    name        varchar(100)                not null,
    category_id int                         null,
    unit        varchar(20)                 not null,
    price       decimal(12, 2) default 0.00 not null,
    VAT         decimal(6, 2)  default 0.19 not null,
    primary key (id),
    constraint shop_product_shop_category_id_fk
        foreign key (category_id) references ami_zone.shop_category (id)
);
insert into ami_zone.shop_product (id, name, category_id, unit, price, VAT)
values  (1, 'Spinach', 1, 'PK', 1.99, 0.07),
        (2, 'Four Cheese Pizza', 1, 'PC', 2.39, 0.07),
        (3, 'Spinach Pizza', 1, 'PC', 2.29, 0.07),
        (4, 'Fish Fingers', 1, 'PK', 1.99, 0.07),
        (5, 'Pasta Pan', 1, 'PK', 3.29, 0.07),
        (6, 'Carrots', 2, 'KG', 0.39, 0.07),
        (7, 'Onions', 2, 'KG', 0.29, 0.07),
        (8, 'Bananas', 2, 'KG', 1.29, 0.07),
        (9, 'Garlic', 2, 'KG', 0.98, 0.07),
        (10, 'Pork Sausage', 3, 'PC', 1.99, 0.07),
        (11, 'Meatballs', 3, 'PK', 2.35, 0.07),
        (12, 'Milk', 4, 'PC', 0.79, 0.07),
        (13, 'Quark', 4, 'PC', 0.99, 0.07),
        (14, 'Yoghurt', 4, 'PC', 0.98, 0.07),
        (15, 'Cola light', 5, 'PC', 1.50, 0.19),
        (16, 'Nerd Bull', 5, 'PC', 1.50, 0.19),
        (17, 'Ice Tea', 5, 'PC', 1.80, 0.19),
        (18, 'Water', 5, 'PC', 0.99, 0.19),
        (19, 'Grill Sausages', 6, 'PK', 3.55, 0.07),
        (20, 'Meatloaf', 6, 'PK', 2.79, 0.07),
        (21, 'Fricandels', 6, 'PK', 3.49, 0.07),
        (22, 'Chips', 7, 'PK', 1.99, 0.07),
        (23, 'Salt Sticks', 7, 'PK', 1.79, 0.07),
        (24, 'Jelly Babies', 7, 'PK', 1.59, 0.07),
        (25, 'Tea', 8, 'PC', 2.50, 0.19),
        (26, 'Coffee', 8, 'PC', 3.20, 0.19),
        (27, 'Pasta in Sauce', 9, 'PK', 1.99, 0.07),
        (28, 'Ravioli', 9, 'PK', 1.89, 0.07),
        (29, 'Fried noodles', 9, 'PK', 2.39, 0.07),
        (30, 'Rice sweet/sour', 9, 'PK', 2.19, 0.07),
        (31, 'Rice patties', 10, 'PC', 2.00, 0.07),
        (32, 'Apple Pie', 10, 'PC', 2.20, 0.07),
        (33, 'Sandwich', 10, 'PC', 2.80, 0.07),
        (34, 'Brown Bread', 10, 'PC', 1.90, 0.07),
        (35, 'Bild', 11, 'PC', 0.90, 0.07),
        (36, 'Kicker', 11, 'PC', 2.00, 0.07),
        (37, 'c''t', 11, 'PC', 3.70, 0.07),
        (38, 'Computerbild', 11, 'PC', 2.20, 0.07),
        (39, 'Zeit', 11, 'PC', 4.20, 0.07),
        (40, 'GameStar', 11, 'PC', 6.50, 0.07);
-- select if(count(id)=40 and max(id)=40,'ok','nok') from ami_zone.shop_product;

-- customer
create table if not exists ami_zone.shop_customer
(
    id                   int                        not null,
    brand                varchar(100)               not null,
    discount_percent     decimal(6, 2) default 0.00 not null,
    risk_of_loss_percent decimal(6, 2)              null,
    primary key (id)
);
insert into ami_zone.shop_customer (id, brand, discount_percent, risk_of_loss_percent)
values  (1, 'Bayer', 1.00, null),
        (2, 'BMW', 1.00, null),
        (3, 'Commerzbank', 0.00, null),
        (4, 'Daimler', 1.00, null),
        (5, 'Deutsche Bank', 0.00, null),
        (6, 'Börse', 0.00, null),
        (7, 'E.ON', 1.00, null),
        (8, 'Infinion', 2.00, null),
        (9, 'Lufthansa', 2.00, null),
        (10, 'RWE', 1.00, 2.00),
        (11, 'SAP', 2.00, null),
        (12, 'Sparkasse', 10.00, null);
-- select if(count(id)=12 and max(id)=12,'ok','nok') from ami_zone.shop_customer;

-- connected_to
create table if not exists ami_zone.shop_connected_to
(
    id            int not null,
    customer_a_id int null,
    customer_b_id int null,
    primary key (id),
    constraint shop_connected_to_shop_customer_id_fk
        foreign key (customer_a_id) references ami_zone.shop_customer (id)
            on update cascade on delete set null,
    constraint shop_connected_to_shop_customer_id_fk2
        foreign key (customer_b_id) references ami_zone.shop_customer (id)
            on update cascade on delete set null
);
insert into ami_zone.shop_connected_to (id, customer_a_id, customer_b_id)
values  (1001, 1, 6),
        (1002, 9, 6),
        (1003, 12, 6),
        (1004, 5, 12),
        (1005, 3, 5),
        (1006, 3, 12),
        (1007, 4, 12),
        (1008, 2, 4),
        (1009, 7, 10),
        (1010, 10, 11),
        (1011, 7, 11),
        (1012, 11, 8);
-- select if(count(id)=12 and max(id)=1012,'ok','nok') from ami_zone.shop_connected_to;

-- order
create table if not exists ami_zone.shop_order
(
    id            int  not null,
    customer_id   int  null,
    employee_id   int  null, -- comment 'agent',
    delivery_time time not null,
    primary key (id),
    constraint shop_order_hr_employee_id_fk
        foreign key (employee_id) references ami_zone.hr_employee (id)
            on update cascade on delete set null,
    constraint shop_order_shop_customer_id_fk
        foreign key (customer_id) references ami_zone.shop_customer (id)
            on update cascade on delete set null
);
insert into ami_zone.shop_order (id, customer_id, employee_id, delivery_time)
values  (1, 12, 10, '13:14:00'),
        (2, 12, 11, '17:38:00'),
        (3, 7, 10, '08:22:00');
-- select if(count(id)=3 and max(id)=3,'ok','nok') from ami_zone.shop_order;

-- consists_of
create table if not exists ami_zone.shop_consists_of
(
    id         int                         not null,
    order_id   int                         null,
    product_id int                         null,
    units      decimal(12, 2) default 1.00 not null,
    primary key (id),
    constraint consists_of_product_id_fk
        foreign key (product_id) references ami_zone.shop_product (id)
            on update cascade on delete set null
);
insert into ami_zone.shop_consists_of (id, order_id, product_id, units)
values  (1, 1, 15, 12.00),
        (2, 1, 16, 6.00),
        (3, 1, 18, 24.00),
        (4, 2, 22, 10.00),
        (5, 2, 23, 5.00),
        (6, 2, 24, 5.00),
        (7, 2, 11, 20.00),
        (8, 3, 15, 6.00),
        (9, 3, 17, 12.00),
        (10, 3, 18, 12.00);
-- select if(count(id)=10 and max(id)=10,'ok','nok') from ami_zone.shop_consists_of;

-- -- ami_zone done -- --
