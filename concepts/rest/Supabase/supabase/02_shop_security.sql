-- (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

-- Step 2 of 2: who may read what. Run after 01_shop_schema.sql (or after ami_zone_full.sql).
--
-- Every request from the app runs in the database as one of two roles:
--   anon           - no user signed in (only the anon key)
--   authenticated  - a user is signed in (the app sends the user's token)
-- Two layers decide what such a request may do:
--   1. GRANT: may this role use the schema / the table at all?
--   2. ROW LEVEL SECURITY (RLS): which rows? With RLS on and no policy, a query returns no rows -
--      not an error. That is the most common 'my list is empty' surprise.
-- Here: signed-in users may read both tables, nobody may write via the API, anon gets nothing.

grant usage on schema ami_zone to authenticated;
grant select on ami_zone.shop_category, ami_zone.shop_product to authenticated;

alter table ami_zone.shop_category enable row level security;
alter table ami_zone.shop_product  enable row level security;

-- 'using (true)': every row is visible. A real policy would filter, e.g. by auth.uid().
drop policy if exists "signed-in users can read categories" on ami_zone.shop_category;
create policy "signed-in users can read categories"
    on ami_zone.shop_category for select
    to authenticated
    using (true);

drop policy if exists "signed-in users can read products" on ami_zone.shop_product;
create policy "signed-in users can read products"
    on ami_zone.shop_product for select
    to authenticated
    using (true);

-- Not SQL, but needed as well: the schema must be exposed to the API, otherwise every request
-- fails with "The schema must be one of the following: public, graphql_public".
-- Dashboard -> Project Settings -> Data API -> Exposed schemas -> add 'ami_zone' -> Save.
