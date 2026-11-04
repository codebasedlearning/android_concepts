// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_app.data.Picture
import de.fh_aachen.android.mvvm_supabase_app.data.PictureRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// A picture together with a signed URL for its file.
data class PictureItem(val picture: Picture, val url: String)

// All pictures of the table, with signed URLs (fetched in parallel).
class PicturesViewModel(private val pictures: PictureRepository) : LoadViewModel<List<PictureItem>>() {

    init { reload() }

    override suspend fun load(): List<PictureItem> = coroutineScope {
        pictures.pictures()
            .map { picture -> async { PictureItem(picture, pictures.signedUrl(picture.objectPath)) } }
            .awaitAll()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PicturesViewModel(container().pictureRepository) }
        }
    }
}
