// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.fh_aachen.android.permissions.ui.theme.FirstAppTheme
import de.fh_aachen.android.permissions.R.drawable.icon_home
import de.fh_aachen.android.permissions.R.drawable.background_castle
import de.fh_aachen.android.permissions.R.drawable.background_permission
import de.fh_aachen.android.ui_tools.HeadlineText
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.PermissionUiState
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.computePermissionUiState
import de.fh_aachen.android.ui_tools.gotoSettingsActivity
import de.fh_aachen.android.ui_tools.isPermissionGranted
import de.fh_aachen.android.ui_tools.navScreensOf

/*
 * You can also use 'adb' to manipulate the permissions:
 *      adb shell pm grant de.fh_aachen.android.permissions android.permission.CAMERA
 *      adb shell pm revoke de.fh_aachen.android.permissions android.permission.CAMERA
 * (adb lives in <Android SDK>/platform-tools, put it on your PATH)
 * or general
 *      adb shell pm revoke your.package.name android.permission.the_one
 * Be aware that the app might be closed after the permission is revoked.
 *
 * A permission is not only granted or denied. In some cases the user should get
 * a so-called 'rationale'. This is essentially an explanation or justification
 * provided to the user, explaining why the app needs a particular permission.
 * This helps users understand the purpose of the permission, making them more
 * likely to accept it if they see a clear reason behind the request.
 */

/*
 * Topics:
 *  - Permissions, State
 *  - Intent
 *  - adb
 */

enum class Screen { Home, PermissionAndroid, PermissionAccompanist, Camera }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(icon_home, background_castle) { LoginScreen() },
                        Screen.PermissionAndroid to NavScreen(R.drawable.outline_counter_1_24, background_permission) { PermissionScreenAndroid() },
                        Screen.PermissionAccompanist to NavScreen(R.drawable.outline_counter_2_24, background_permission) { PermissionScreenAccompanist() },
                        Screen.Camera to NavScreen(R.drawable.icon_camera, background_castle) { CameraScreen() },
                    )
                )
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navController.navigate(Screen.PermissionAndroid.name) }) {
                Text("Android Permission Dialog", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.PermissionAccompanist.name) }) {
                Text("Accompanist Permission Dialog", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { gotoSettingsActivity(context) }) {
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var camGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA,context)) }

    // the user may change the permission in the system settings while we are in the background,
    // so check again whenever the screen is resumed
    LifecycleResumeEffect(Unit) {
        camGranted = isPermissionGranted(Manifest.permission.CAMERA, context)
        onPauseOrDispose { }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RoundedRectangle {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeadlineText("Cam Mockup (Perm. $camGranted)")
                Button(onClick = { gotoSettingsActivity(context) }) {
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
fun GrantedBlock(
    // onPermissionGranted: () -> Unit,
) {
    val navController = LocalNavController.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Camera permission granted ✅")
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate(Screen.Camera.name) } /* or onPermissionGranted */) {
            Text("Continue")
        }
    }
}

@Composable
fun NotGrantedRequestableBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("We need camera permission to continue.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = action) {
            Text("Allow camera")
        }
    }
}

@Composable
fun NotGrantedShowRationaleBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("You denied camera before.\nWe use it to take photos.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = action) {
            Text("Try again")
        }
    }
}

@Composable
fun NotGrantedMaybePermanentlyDeniedBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Camera permission seems blocked.\n Please enable it in system settings.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = action ) {
            Text("Open settings")
        }
    }
}

@Composable
fun PermissionScreenAndroid(
    // if you want to provide action, do it this way
    // onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    // LocalActivity instead of 'context as Activity' (LocalContext may be a ContextWrapper)
    val activity = LocalActivity.current ?: return
    val permission = Manifest.permission.CAMERA

    // single flag we track ourselves
    var hasEverRequested by rememberSaveable { mutableStateOf(false) }

    // The UI state is a snapshot of something Compose cannot observe (the permission
    // database), so we keep it in a state and recompute it whenever it may have changed:
    // after the dialog returns and whenever the screen is resumed (e.g. back from Settings).
    fun computeUiState() = computePermissionUiState(
        permission = permission,
        context = context,
        activity = activity,
        hasEverRequested = hasEverRequested
    )
    var uiState by remember { mutableStateOf(computeUiState()) }

    LifecycleResumeEffect(Unit) {
        uiState = computeUiState()
        onPauseOrDispose { }
    }

    /*
     * rememberLauncherForActivityResult is a composable function that provides
     * a convenient and lifecycle-aware way to launch activities for results.
     * This includes things like opening the camera, picking files, or requesting
     * permissions.
     * It allows you to launch an activity and handle its result within the
     * Compose UI without needing the traditional Activity or Fragment APIs.
     */
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasEverRequested = true
        uiState = computeUiState()
        // if (isGranted) onPermissionGranted()
    }

    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopCenter) {
        RoundedRectangle {
            when (uiState) {
                PermissionUiState.Granted -> {
                    GrantedBlock(/* onPermissionGranted */)
                }
                PermissionUiState.NotGrantedRequestable -> {
                    NotGrantedRequestableBlock {
                        permissionLauncher.launch(permission)   // the result callback updates the state
                    }
                }
                PermissionUiState.NotGrantedShowRationale -> {
                    NotGrantedShowRationaleBlock {
                        permissionLauncher.launch(permission)
                    }
                }
                PermissionUiState.NotGrantedMaybePermanentlyDenied -> {
                    NotGrantedMaybePermanentlyDeniedBlock {
                        gotoSettingsActivity(context)
                    }
                }
            }
        }
    }
}

/*
 * Accompanist only tells us 'granted', 'denied' and 'shouldShowRationale'. "Denied without
 * rationale" means either "never asked" or "don't ask again" - the same ambiguity as above,
 * so we need our own hasEverRequested flag here, too.
 */
@OptIn(ExperimentalPermissionsApi::class)
fun computePermissionUiStateAccompanist(status: PermissionStatus, hasEverRequested: Boolean): PermissionUiState =
    when (status) {
        PermissionStatus.Granted -> PermissionUiState.Granted
        is PermissionStatus.Denied -> when {
            status.shouldShowRationale -> PermissionUiState.NotGrantedShowRationale
            hasEverRequested -> PermissionUiState.NotGrantedMaybePermanentlyDenied
            else -> PermissionUiState.NotGrantedRequestable
        }
    }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreenAccompanist(
    // onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    var hasEverRequested by rememberSaveable { mutableStateOf(false) }
    // the callback runs when the dialog returns; status itself is observable state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) {
        hasEverRequested = true
    }

    val uiState = computePermissionUiStateAccompanist(cameraPermissionState.status, hasEverRequested)

    /*
     * LaunchedEffect (see below) is a composable function in Jetpack Compose that
     * allows you to run suspendable side effects (like coroutine operations) in response
     * to changes in the composition.
     * It is composition-aware, not lifecycle-aware: the coroutine is cancelled when the
     * composable leaves the composition or the key changes, but it keeps running while the
     * app is in the background. Lifecycle-aware variants are LifecycleStartEffect,
     * LifecycleResumeEffect, repeatOnLifecycle or collectAsStateWithLifecycle.
     */

    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopCenter) {
        RoundedRectangle {
            when (uiState) {
                PermissionUiState.Granted -> {
                    // UI
                    GrantedBlock(/* onPermissionGranted */)
                    // or call the callback once: LaunchedEffect(Unit) { onPermissionGranted() }
                }
                PermissionUiState.NotGrantedRequestable -> {
                    NotGrantedRequestableBlock {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                PermissionUiState.NotGrantedShowRationale -> {
                    NotGrantedShowRationaleBlock {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                PermissionUiState.NotGrantedMaybePermanentlyDenied -> {
                    NotGrantedMaybePermanentlyDeniedBlock {
                        gotoSettingsActivity(context)
                    }
                }
            }
        }
    }
}
