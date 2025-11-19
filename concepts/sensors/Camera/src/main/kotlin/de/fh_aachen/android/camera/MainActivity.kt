// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import de.fh_aachen.android.camera.ui.theme.FirstAppTheme
import de.fh_aachen.android.camera.R.drawable.icon_home
import de.fh_aachen.android.camera.R.drawable.icon_permission
import de.fh_aachen.android.camera.R.drawable.icon_camera
import de.fh_aachen.android.camera.R.drawable.background_castle
import de.fh_aachen.android.camera.R.drawable.background_permission
import de.fh_aachen.android.camera.R.drawable.background_camera
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
You can also use 'adb' to manipulate the permissions:
    /Users/voss/Library/Android/sdk/platform-tools/adb shell pm revoke de.fh_aachen.android.camera android.permission.CAMERA
or general
    adb shell pm revoke your.package.name android.permission.the_one
Be aware that the app might be closed after the permission is revoked.

A permission is not only granted or denied. In some cases the user should get
a so-called 'rationale'. This is essentially an explanation or justification provided to the user,
explaining why the app needs a particular permission. This helps users understand the purpose of
the permission, making them more likely to accept it if they see a clear reason behind the request.
*/

enum class Screen { Home, Permission, Camera }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(icon_home, background_castle) { LoginScreen() },
                        Screen.Permission to NavScreen(icon_permission, background_permission) { PermissionScreen() },
                        Screen.Camera to NavScreen(icon_camera, background_camera) { CameraScreen() },
                    )
                )
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { navController.navigate(Screen.Permission.name) }) {
            Text("Camera - Login", fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
    }
}

fun isCameraPermissionGranted(context: Context)
    = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen() {

    // Version 1
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(isCameraPermissionGranted(context)) }
    /*
    rememberLauncherForActivityResult is a composable function that provides a convenient and
    lifecycle-aware way to launch activities for results. This includes things like opening
    the camera, picking files, or requesting permissions.
    It allows you to launch an activity and handle its result within the Compose UI
    without needing the traditional Activity or Fragment APIs.
    */
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    // Version 2 - ExperimentalPermissionsApi
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    /*
    LaunchedEffect is a composable function in Jetpack Compose that allows you to run
    suspendable side effects (like coroutine operations) in response to changes in the composition.
    It provides a way to launch coroutines within the composable scope in a safe,
    lifecycle-aware way.

    Here, hasCameraPermission does not update automatically when cameraPermissionState changes,
    but with this little helper it does.
    */
    LaunchedEffect(cameraPermissionState.status) {
        hasCameraPermission = isCameraPermissionGranted(context)
    }

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xeeff0000)).padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Permission Camera", fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status V1, granted: $hasCameraPermission", fontSize = 24.sp, color = Color.White)
                    Text("Status V2, granted: ${cameraPermissionState.status.isGranted}", fontSize = 24.sp, color = Color.White)
                    Text("Status V2, rationale: ${cameraPermissionState.status.shouldShowRationale}", fontSize = 24.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xccff8000)).padding(2.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(onClick = {
                        launcher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Request V1")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text("Request V2")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    if (!isCameraPermissionGranted(context)) {
        val navController = LocalNavController.current
        navController.navigate(Screen.Permission.name)
    }

    // see also Manifest for permissions and file providers
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedImage = imageUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream).asImageBitmap()
                }
            }
        }
    }

    fun createImageFile(context: Context): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
    ) {
        capturedImage?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Captured Image",
                modifier = Modifier.size(200.dp).padding(16.dp)
            )
        }

        Button(
            onClick = {
                imageUri = createImageFile(context)
                launcher.launch(imageUri!!)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Capture Image")
        }
    }
}
