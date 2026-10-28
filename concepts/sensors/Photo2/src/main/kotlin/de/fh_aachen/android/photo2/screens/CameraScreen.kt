// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo2.screens

import androidx.compose.ui.res.stringResource
import de.fh_aachen.android.photo2.R
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner     // moved here from androidx.compose.ui.platform
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.gotoSettingsActivity
import de.fh_aachen.android.ui_tools.isPermissionGranted
import kotlinx.coroutines.awaitCancellation
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "PHOTO2"

/*
 *  CameraX, unlike the camera intent (Photo1), uses the camera itself, so it needs the
 *  CAMERA runtime permission. Before, the request was commented out ('grant permission manually
 *  for the moment') and without it the preview simply stayed black. Now we ask on entering the
 *  screen, and show a hint (plus Settings) as long as it is not granted.
 */
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA, context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> granted = isGranted }

    // ask once when the screen appears (a side effect, so not in the composable body)
    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (granted) {
        CameraPreviewWithCapture()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangle {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.permission_missing))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text(stringResource(R.string.allow_camera)) }
                    Button(onClick = { gotoSettingsActivity(context) }) { Text(stringResource(R.string.settings)) }
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewWithCapture() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    // The use case for taking photos lives here now (was: in the Activity).
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Set up camera when PreviewView is ready
    LaunchedEffect(previewView) {
        val view = previewView ?: return@LaunchedEffect
        // suspends until CameraX is initialised; getInstance(context).get() would block the main thread
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {            // e.g. no back camera (emulator settings)
            Log.e(TAG, "binding the camera failed", e)
            return@LaunchedEffect
        }

        /*
         * Release the camera when this screen leaves the composition.
         *  bindToLifecycle ties the camera to 'lifecycleOwner'. Under Navigation 2 that was the
         *  back stack entry, which was destroyed when you navigated away. With Navigation 3 (no
         *  lifecycle per entry) it is the Activity - so the camera kept running after leaving the
         *  screen. Waiting here until the effect is cancelled and unbinding in 'finally' makes the
         *  screen clean up after itself, whatever the navigation does.
         */
        try {
            awaitCancellation()
        } finally {
            cameraProvider.unbindAll()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    pv.scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = pv
                }
            }
        )

        Button(
            onClick = { takePhoto(context, imageCapture) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(stringResource(R.string.capture))
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Compose")
        }
    }

    // MediaStore without permission works from Android 10 (API 29) on; on API 27/28 this
    // would need WRITE_EXTERNAL_STORAGE (not covered here, the app is a temp version anyway).
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, context.getString(R.string.capture_failed, exc.message), Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, context.getString(R.string.photo_saved, output.savedUri), Toast.LENGTH_SHORT).show()
            }
        }
    )
}
