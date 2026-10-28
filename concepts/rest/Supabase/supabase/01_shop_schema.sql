-- (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

-- Step 1 of 2: the tables the Supabase app reads - a schema and two tables with sample data.
-- Run it in the Supabase dashboard: SQL Editor -> New query -> paste -> Run.
-- It can be run again: tables are only created if missing, rows only inserted if missing.
-- (ami_zone_full.sql contains the complete course database, including these two tables.)

create schema if not exists ami_zone;

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
        (12, 'Services')
on conflict (id) do nothing;

-- product; 'category_id' is the foreign key that the app follows with 'category:shop_category(*)'
create table if not exists ami_zone.shop_product
(
    id          int                         not null,
    name        varchar(100)                not null,
    category_id int                         null,
    unit        varchar(20)                 not null,
    price       decimal(12, 2) default 0.00 not null,
    vat         decimal(6, 2)  default 0.19 not null,
    primary key (id),
    constraint shop_product_shop_category_id_fk
        foreign key (category_id) references ami_zone.shop_category (id)
);

insert into ami_zone.shop_product (id, name, category_id, unit, price, vat)
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
        (40, 'GameStar', 11, 'PC', 6.50, 0.07)
on conflict (id) do nothing;

-- check: 12 categories, 40 products
select (select count(*) from ami_zone.shop_category) as categories,
       (select count(*) from ami_zone.shop_product)  as products;
