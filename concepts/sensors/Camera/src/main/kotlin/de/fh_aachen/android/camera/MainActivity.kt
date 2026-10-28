// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.camera

import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import de.fh_aachen.android.camera.R.drawable.icon_home
import de.fh_aachen.android.camera.R.drawable.icon_permission
import de.fh_aachen.android.camera.R.drawable.icon_camera
import de.fh_aachen.android.camera.R.drawable.background_castle
import de.fh_aachen.android.camera.R.drawable.background_permission
import de.fh_aachen.android.camera.R.drawable.background_camera
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.gotoSettingsActivity
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
You can also use 'adb' to manipulate the permissions:
    adb shell pm revoke de.fh_aachen.android.camera android.permission.CAMERA
or general
    adb shell pm revoke your.package.name android.permission.the_one
Be aware that the app might be closed after the permission is revoked.

A permission is not only granted or denied. In some cases the user should get
a so-called 'rationale'. This is essentially an explanation or justification provided to the user,
explaining why the app needs a particular permission. This helps users understand the purpose of
the permission, making them more likely to accept it if they see a clear reason behind the request.

Note on TakePicture (ACTION_IMAGE_CAPTURE): the camera app takes the photo, so normally no permission
is needed (see Photo1). But if an app *declares* CAMERA in its manifest, like this one, the intent
throws a SecurityException unless CAMERA is granted. That's why CameraScreen checks it first.
*/

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object Permission : Screen
    @Serializable data object Camera : Screen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                CameraApp()
            }
        }
    }
}

/*
 *  No automatic redirect to the permission screen any more (it was a LaunchedEffect
 *   that navigated as soon as the permission was missing, and Back could loop). 'Permission
 *   missing' is state: the camera screen shows a hint with a button, the user decides. And the
 *   permission screen reports 'granted', so the host can lead back to the camera.
 */
@Composable
fun CameraApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Permission, icon_permission, stringResource(R.string.nav_permission)),
            ConceptDestination(Screen.Camera, icon_camera, stringResource(R.string.nav_camera)),
        ),
        selected = backStack.lastOrNull(),
        onSelect = { backStack.navigateToTopLevel(it) },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(padding),
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider<NavKey> {
                entry<Screen.Home> {
                    ScreenWithBackground(background_castle) {
                        LoginScreen(onLogin = { backStack.navigateToTopLevel(Screen.Permission) })
                    }
                }
                entry<Screen.Permission> {
                    ScreenWithBackground(background_permission) {
                        PermissionScreen(onPermissionGranted = { backStack.navigateToTopLevel(Screen.Camera) })
                    }
                }
                entry<Screen.Camera> {
                    ScreenWithBackground(background_camera) {
                        CameraScreen(onOpenPermission = { backStack.navigateToTopLevel(Screen.Permission) })
                    }
                }
            }
        )
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onLogin) {
            Text(stringResource(R.string.login), fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
    }
}

fun isCameraPermissionGranted(context: Context)
    = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {     // reports 'granted' to the host

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
    It is composition-aware (cancelled when it leaves the composition or the key changes),
    not lifecycle-aware: it keeps running while the app is in the background.

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
                    Text(stringResource(R.string.permission_title), fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
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
                    Button(onClick = { gotoSettingsActivity(context) }) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
            // once granted, offer the way to the camera
            if (hasCameraPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPermissionGranted) {
                    Text(stringResource(R.string.continue_to_camera))
                }
            }
        }
    }
}

// Decodes a down-sampled bitmap (1/sampleSize per side); a full-resolution photo can be
// 50+ MB as a Bitmap. Call it off the main thread.
private fun decodeSampled(context: Context, uri: Uri, sampleSize: Int = 4): ImageBitmap? =
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeStream(inputStream, null, options)?.asImageBitmap()
    }

/*
 * A trap worth knowing: taking a photo with the camera intent (TakePicture, i.e.
 *  ACTION_IMAGE_CAPTURE) needs no permission at all - the system camera app takes the photo,
 *  not us. But: if an app declares CAMERA in its manifest (as this one does, to demonstrate the
 *  permission screen) and the permission is not granted, the intent throws a SecurityException.
 *  So here the permission is needed only because we declare it. Photo1 does the same without
 *  declaring it, and needs no permission at all.
 */
@Composable
fun CameraScreen(onOpenPermission: () -> Unit) {
    val context = LocalContext.current

    // Re-checked on every resume (e.g. back from the system settings). Before, it was
    //  checked once, and a missing permission triggered a navigation from a LaunchedEffect.
    var granted by remember { mutableStateOf(isCameraPermissionGranted(context)) }
    LifecycleResumeEffect(Unit) {
        granted = isCameraPermissionGranted(context)
        onPauseOrDispose { }
    }

    if (!granted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangle {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.permission_missing))      // why it is needed here: see the comment above
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenPermission) { Text(stringResource(R.string.grant_permission)) }
                }
            }
        }
        return
    }

    // see also Manifest for permissions and file providers
    // rememberSaveable: the camera app runs in another process; if our Activity is recreated
    // meanwhile (rotation, process death), a plain 'remember' would lose the target URI.
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var captureCount by rememberSaveable { mutableIntStateOf(0) }
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) captureCount++
        // cancelled or failed: remove the empty file we created for the camera
        //  (FileProvider supports delete), otherwise every cancelled capture leaves one behind
        else imageUri?.let { context.contentResolver.delete(it, null, null) }
    }

    // decode in the background whenever a new photo was taken (and again after recreation)
    LaunchedEffect(captureCount) {
        val uri = imageUri ?: return@LaunchedEffect
        if (captureCount == 0) return@LaunchedEffect
        capturedImage = withContext(Dispatchers.IO) { decodeSampled(context, uri) }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
    ) {
        capturedImage?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.captured_image),
                modifier = Modifier.size(200.dp).padding(16.dp)
            )
        }

        Button(
            onClick = {
                val uri = createImageUri(context)       // no '!!' needed: the helper never returns null
                imageUri = uri
                launcher.launch(uri)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.capture_image))
        }
    }
}

//  moved out of the composable (as a local fun it was re-created on every recomposition),
//  and with a non-null return type, so the caller needs no '!!'
private fun createImageUri(context: Context): Uri {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}
