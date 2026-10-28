# Supabase setup for `concepts/rest/Supabase`

What the app needs on the Supabase side, step by step. Everything happens in the
Supabase dashboard, apart from the SQL scripts, which you run in its SQL editor.

## 1. Project and keys

1. Create a project at [supabase.com](https://supabase.com) (the free tier is enough).
2. In *Project Settings → API keys* (or the *Connect* dialog), note:
   - the project URL, e.g. `https://abcdefgh.supabase.co`, and
   - the **anon** (public) key.
3. Copy the `supabase_*` lines from `local.properties.example` (project root) into your
   `local.properties` and fill in:
   ```properties
   supabase_url=abcdefgh.supabase.co          # without https://, the app adds it
   supabase_anon_key=eyJ...                   # the anon key, never the service_role key
   ```
   The anon key is meant for clients. Your data is protected by row level security
   (step 3), not by keeping this key secret. The `service_role` key bypasses all of
   that and must never end up in an app.

## 2. Tables

In *SQL Editor → New query*, run **`01_shop_schema.sql`**. It creates the schema
`ami_zone` with `shop_category` and `shop_product` plus sample data. The script can
be run again safely.

*Alternative:* `ami_zone_full.sql` is the complete course database (departments,
employees, assets, shop, …), including the two shop tables. Run either that one
**or** `01_shop_schema.sql`, not both, since the full script inserts without
`on conflict` and would fail on rows that already exist.

## 3. Rights

1. Run **`02_shop_security.sql`**: grants plus row level security, so signed-in
   users may read both tables. Without a matching policy, a query returns *no rows*,
   with no error.
2. Expose the schema to the API: *Project Settings → Data API → Exposed schemas* →
   add `ami_zone` → *Save*. Without this, every request fails with
   "The schema must be one of the following: public, graphql_public".

## 4. A user

*Authentication → Users → Add user → Create new user*: email and password, with
*Auto Confirm User* on. Put both into `local.properties`; the app pre-fills its
sign-in form with them (a convenience for this demo only):
```properties
supabase_user_email=student@example.com
supabase_user_password=...
```
Rebuild the app after changing `local.properties`, since the values are compiled
into `BuildConfig`.

## 5. Realtime

Broadcast on a public channel works with the anon key, so there's nothing to set
up. If messages don't arrive, check *Realtime → Settings*: public access must be
allowed for this example. Private channels need their own policies, which is
beyond this demo.

Try it with **`connect.py`**:
```bash
python -m venv .venv && source .venv/bin/activate
pip install supabase python-dotenv "psycopg[binary]"
python connect.py      # type a key + Enter to send; the app's 'Send ping' shows up here
```
For `connect_postgres_db()` (commented out in `main`) you also need the database
password and the `supabase_db_*` lines from `local.properties.example`. That's for
testing only; an app never connects to Postgres directly.

## Files

| File | What it is |
| --- | --- |
| `01_shop_schema.sql` | schema `ami_zone` with `shop_category`, `shop_product` and data |
| `02_shop_security.sql` | grants and row level security for signed-in users |
| `99_drop.sql` | removes the shop tables (or, commented out, the whole schema) |
| `ami_zone_full.sql` | the complete course database (from `android_course`) |
| `connect.py` | desktop counterpart: SQL, REST and Realtime from Python |
