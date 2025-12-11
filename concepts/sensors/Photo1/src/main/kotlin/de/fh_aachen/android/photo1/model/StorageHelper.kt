package de.fh_aachen.android.photo1.model

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun createImageFileInAppStorage(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val fileName = "IMG_$timeStamp.jpg"
    val dir = File(context.filesDir, "photos")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, fileName)
}


fun createImageUriInMediaStore(context: Context): Uri? {
    val timeStamp =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val fileName = "IMG_$timeStamp.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            put(MediaStore.Images.Media.IS_PENDING, 1)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Compose")
        }
    }

    val resolver = context.contentResolver
    val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    val uri = resolver.insert(collection, contentValues)

    if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Mark as not pending after capture – simplest is: we assume the camera writes immediately
        contentValues.clear()
        //contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    }

    return uri
}
