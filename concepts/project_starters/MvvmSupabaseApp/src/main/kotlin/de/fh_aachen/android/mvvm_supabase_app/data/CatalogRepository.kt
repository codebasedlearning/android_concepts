// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

// Reads the catalog from the views category_stats and product_details.
class CatalogRepository(private val supabase: SupabaseClient) {

    suspend fun categories(): List<CategoryStats> =
        supabase.from("category_stats")
            .select { order("name", Order.ASCENDING) }
            .decodeList<CategoryStats>()

    suspend fun products(categoryId: Int): List<ProductDetails> =
        supabase.from("product_details")
            .select {
                filter { eq("category_id", categoryId) }
                order("name", Order.ASCENDING)
            }
            .decodeList<ProductDetails>()

    suspend fun product(id: Int): ProductDetails =
        supabase.from("product_details")
            .select { filter { eq("id", id) } }
            .decodeSingle<ProductDetails>()
}
