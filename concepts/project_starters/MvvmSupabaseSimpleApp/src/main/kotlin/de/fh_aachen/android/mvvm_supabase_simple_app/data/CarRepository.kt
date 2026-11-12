// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

// Reads and updates the table public.cars directly; no views, no RPC.
// What is allowed is decided in the database: GRANTs + RLS, see sql/cars.sql.
class CarRepository(private val supabase: SupabaseClient) {

    // GET /rest/v1/cars?select=*&order=id.asc
    suspend fun all(): List<Car> =
        supabase.from(TABLE)
            .select { order("id", Order.ASCENDING) }
            .decodeList<Car>()

    // PATCH /rest/v1/cars?id=eq.<id>  body {"model": …}; select() returns the changed row.
    // No row back = unknown id, or RLS filtered it out; PostgREST reports that as success with [].
    suspend fun renameModel(id: Int, model: String): Car =
        supabase.from(TABLE)
            .update({ set("model", model) }) {
                select()
                filter { eq("id", id) }
            }
            .decodeList<Car>()
            .singleOrNull()
            ?: error("car $id was not updated (unknown id, or not allowed)")

    private companion object {
        const val TABLE = "cars"
    }
}
