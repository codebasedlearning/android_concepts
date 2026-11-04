// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Rows of the API views (public schema). Names follow the columns, @SerialName where they differ.

// view category_stats
@Serializable
data class CategoryStats(
    val id: Int,
    val name: String,
    val products: Int = 0,
    @SerialName("min_price") val minPrice: Double? = null,
    @SerialName("max_price") val maxPrice: Double? = null,
)

// view product_details (price is gross, net and VAT are derived in the view)
@Serializable
data class ProductDetails(
    val id: Int,
    val name: String,
    val unit: String,
    val price: Double,
    @SerialName("vat_percent") val vatPercent: Double,
    @SerialName("price_net") val priceNet: Double,
    @SerialName("vat_amount") val vatAmount: Double,
    val category: String? = null,
    @SerialName("picture_count") val pictureCount: Int = 0,
    @SerialName("primary_picture_path") val primaryPicturePath: String? = null,
)

// view pictures (metadata; the file itself is in the storage bucket)
@Serializable
data class Picture(
    val id: Long,
    @SerialName("object_path") val objectPath: String,
    @SerialName("file_name") val fileName: String,
    val title: String? = null,
    @SerialName("product_name") val productName: String? = null,
)

// view messages ('mine' = written by the signed-in user)
@Serializable
data class ChatMessage(
    val id: Long,
    val author: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("edited_at") val editedAt: String? = null,
    val mine: Boolean = false,
)
