// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo2

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import de.fh_aachen.android.photo2.screens.HomeScreen
import de.fh_aachen.android.photo2.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.camera.core.*
import de.fh_aachen.android.photo2.screens.CameraScreen

/*
 * We use the UI library to encapsulate the details of the navigation and just provide
 * a couple of screens to (Nav)Scaffold.
 *
 * The screens can be found in 'screens'.
 */
enum class Screen { Home, Camera }

class MainActivity : ComponentActivity() {
    private var imageCapture: ImageCapture? = null

// grant permission manually for the moment

//    private val cameraPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
//            //if (!granted) {
//            //    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
//            //}
//        }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Compose")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Capture failed: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@MainActivity,
                        "Photo saved: ${output.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

//    private fun hasCameraPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                // from UI Tools
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(R.drawable.icon_home, R.drawable.home_city) { HomeScreen() },
                        Screen.Camera to NavScreen(R.drawable.icon_camera, R.drawable.camera_zoo) {
                            CameraScreen(
                                onImageCapture = { capture -> imageCapture = capture },
                                onTakePhoto = { takePhoto() }
                            )
                        },
                    )
                )
            }
        }
    }
}

fun gotoSettingsActivity(
    context: Context,
) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}
