-- (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

-- Remove what 01/02 created. Careful: data is gone afterwards.

-- only the two shop tables (policies and grants go with them):
drop table if exists ami_zone.shop_product;
drop table if exists ami_zone.shop_category;

-- or the whole schema with everything in it (also the tables of ami_zone_full.sql):
-- drop schema if exists ami_zone cascade;
