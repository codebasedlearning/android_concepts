# (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

# A desktop counterpart to the Android app - three small tests of the same Supabase project:
#   connect_postgres_db  - plain SQL over the Postgres protocol (for admin/testing only, never
#                          from an app: it needs the database password)
#   connect_supabase     - the REST API, like the app: sign in, read products
#   connect_realtime     - join the broadcast channel; press a key + Enter to send a message to
#                          the app, and see the app's 'Send ping' arrive here
#
# Install (in a virtual environment):
#   pip install supabase python-dotenv "psycopg[binary]"
# ('dotenv' on PyPI is a different, old package - the right one is python-dotenv.)
# Run:
#   python connect.py

import asyncio
import os
import select
import sys
from pathlib import Path

import psycopg
from dotenv import load_dotenv
from supabase import create_client, acreate_client

# The settings come from the project's local.properties (key=value, as dotenv expects).
#  Found relative to this file (concepts/rest/Supabase/supabase/connect.py -> project root),
#  so the script works from any working directory (before: a path relative to the cwd).
load_dotenv(dotenv_path=Path(__file__).resolve().parents[4] / "local.properties")

# same names as in the app (SupabaseRepository)
CHANNEL = "ami_zone_channel"
EVENT = "my_event"

supabase_url = os.getenv("supabase_url")
supabase_anon_key = os.getenv("supabase_anon_key")

supabase_db_port = os.getenv("supabase_db_port")
supabase_db_database = os.getenv("supabase_db_database")
supabase_db_driver = os.getenv("supabase_db_driver")
supabase_db_user = os.getenv("supabase_db_user")
supabase_db_password = os.getenv("supabase_db_password")

supabase_user_email = os.getenv("supabase_user_email")
supabase_user_password = os.getenv("supabase_user_password")


def connect_postgres_db():
    # do not do this in mobile apps, it is just for testing
    try:
        connection = f"postgresql://{supabase_db_user}:{supabase_db_password}@db.{supabase_url}:{supabase_db_port}/{supabase_db_database}"
        with psycopg.connect(connection) as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT version()")
                print(f" 1| connected to: {cur.fetchone()[0]}")

                print(f" 2| products:")
                cur.execute("SELECT * FROM ami_zone.shop_product")
                for product in cur.fetchall():
                    print(f" a| - {product}")

    except Exception as e:
        print("connect to postgres failed:", e)


def connect_supabase():
    try:
        supabase_http_url = f"https://{supabase_url}"
        client = create_client(supabase_http_url, supabase_anon_key)

        response = client.auth.sign_in_with_password({"email": supabase_user_email, "password": supabase_user_password})
        print(f" 1| connected to: {response}")

        table_query = client.schema("ami_zone").table("shop_product").select("id,name,category:shop_category(*),unit,price,vat")
        response = table_query.execute()

        print(f" 2| result to: {table_query}")
        for product in response.data:
            print(f" a| - {product['name']} in category {product['category']['name']}")
            break

    except Exception as e:
        print("connect to supabase failed:", e)


async def connect_realtime():
    try:
        supabase_http_url = f"https://{supabase_url}"
        client = await acreate_client(supabase_http_url, supabase_anon_key)

        response = None # await client.auth.sign_in_with_password({"email": supabase_user_email, "password": supabase_user_password})
        print(f" 1| connected to: {response}")

        channel = client.realtime.channel(CHANNEL)
        print(f" 2| channel: {channel}")

        def handle_broadcast(payload):
            print(f" 3| handle_broadcast - message received: {payload}")

        def on_subscribe(status, err):
            print(f" 4| on_subscribe - status change: {status}, {err}")

        await channel.on_broadcast(event=EVENT, callback=handle_broadcast).subscribe(on_subscribe)

        print(" 5| type a key + Enter to broadcast, Ctrl+C to stop")
        no = 0
        while True:
            if select.select([sys.stdin], [], [], 0)[0]:
                key = sys.stdin.read(1).strip()
                if key:
                    no = no + 1
                    print(f" 6| send broadcast for {no=}")
                    await channel.send_broadcast(EVENT, {"msg": f"HuHu Android ({key=})", "no": no})
            await asyncio.sleep(0.5)

    except Exception as e:
        print("connect to realtime failed:", e)

if __name__ == '__main__':
    # connect_postgres_db()
    # connect_supabase()
    asyncio.run(connect_realtime())
