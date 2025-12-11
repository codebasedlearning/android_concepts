// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo1.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


const val TAG = "PREFS"

class PhotoViewModel : ViewModel() {

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    fun onPhotoCaptured(uri: Uri) {
        _photoUri.value = uri
    }
}
