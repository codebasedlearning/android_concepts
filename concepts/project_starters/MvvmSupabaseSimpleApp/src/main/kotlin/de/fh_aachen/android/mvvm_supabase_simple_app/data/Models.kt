// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A row of the table public.cars. Names follow the columns, @SerialName where they differ.
@Serializable
data class Car(
    val id: Int,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: Double,
    @SerialName("updated_at") val updatedAt: String? = null,
)
