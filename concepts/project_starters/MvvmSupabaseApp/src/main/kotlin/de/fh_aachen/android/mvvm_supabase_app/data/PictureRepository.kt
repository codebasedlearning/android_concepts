// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.hours

// Picture metadata from the view 'pictures', image files from the private storage bucket.
class PictureRepository(private val supabase: SupabaseClient) {

    suspend fun pictures(): List<Picture> =
        supabase.from("pictures")
            .select { order("id", Order.ASCENDING) }
            .decodeList<Picture>()

    // The bucket is private: images are loaded through short-lived signed URLs.
    suspend fun signedUrl(objectPath: String): String =
        supabase.storage.from(BUCKET).createSignedUrl(objectPath, expiresIn = 1.hours)

    private companion object {
        const val BUCKET = "pictures"
    }
}
