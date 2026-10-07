// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo1.model

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private fun newImageName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    return "IMG_$timeStamp.jpg"
}

// Variant A: app-specific storage (no permission, deleted with the app), shared via FileProvider
fun createImageFileInAppStorage(context: Context): File {
    val dir = File(context.filesDir, "photos")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, newImageName())
}

/*
 * Variant B: shared storage via MediaStore (visible in the Photos/Gallery app).
 *
 * Since Android 10 (API 29) no permission is needed for inserting your own media. The entry
 * exists right after insert(), before the camera has written anything; if the user cancels,
 * finishMediaStoreImage removes it again. (IS_PENDING would hide the entry meanwhile, but the
 * external camera app must be able to write into it, so we keep it simple here.)
 * On API 27/28 (our minSdk is 27) inserting would need the WRITE_EXTERNAL_STORAGE runtime
 * permission, which this demo does not cover - so we return null there.
 */
fun createImageUriInMediaStore(context: Context): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, newImageName())
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Photo1")
    }
    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    return context.contentResolver.insert(collection, contentValues)
}

// Call this after the camera returns: on cancel/failure remove the empty MediaStore entry.
fun finishMediaStoreImage(context: Context, uri: Uri, success: Boolean) {
    if (uri.authority != MediaStore.AUTHORITY) return       // FileProvider URI, nothing to do
    if (!success) context.contentResolver.delete(uri, null, null)
}
