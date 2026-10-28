// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.permissions

import androidx.compose.ui.res.stringResource
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.fh_aachen.android.permissions.R.drawable.icon_home
import de.fh_aachen.android.permissions.R.drawable.background_castle
import de.fh_aachen.android.permissions.R.drawable.background_permission
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.gotoSettingsActivity
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.isPermissionGranted
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

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

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object PermissionAndroid : Screen
    @Serializable data object PermissionAccompanist : Screen
    @Serializable data object Camera : Screen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                PermissionsApp()
            }
        }
    }
}

@Composable
fun PermissionsApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    val openCamera = { backStack.navigateToTopLevel(Screen.Camera) }
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.PermissionAndroid, R.drawable.outline_counter_1_24, stringResource(R.string.android_dialog)),
            ConceptDestination(Screen.PermissionAccompanist, R.drawable.outline_counter_2_24, stringResource(R.string.accompanist_dialog)),
            ConceptDestination(Screen.Camera, R.drawable.icon_camera, stringResource(R.string.nav_camera)),
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
                        LoginScreen(
                            onOpenAndroidDialog = { backStack.navigateToTopLevel(Screen.PermissionAndroid) },
                            onOpenAccompanistDialog = { backStack.navigateToTopLevel(Screen.PermissionAccompanist) },
                        )
                    }
                }
                entry<Screen.PermissionAndroid> {
                    ScreenWithBackground(background_permission) { PermissionScreenAndroid(onPermissionGranted = openCamera) }
                }
                entry<Screen.PermissionAccompanist> {
                    ScreenWithBackground(background_permission) { PermissionScreenAccompanist(onPermissionGranted = openCamera) }
                }
                entry<Screen.Camera> {
                    ScreenWithBackground(background_castle) { CameraScreen() }
                }
            }
        )
    }
}

@Composable
fun LoginScreen(onOpenAndroidDialog: () -> Unit, onOpenAccompanistDialog: () -> Unit) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onOpenAndroidDialog) {
                Text(stringResource(R.string.android_dialog), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenAccompanistDialog) {
                Text(stringResource(R.string.accompanist_dialog), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { gotoSettingsActivity(context) }) {
                Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
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
                Text("Cam Mockup (Perm. $camGranted)", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { gotoSettingsActivity(context) }) {
                    Text(stringResource(R.string.settings))
                }
            }
        }
    }
}

@Composable
fun GrantedBlock(
    onPermissionGranted: () -> Unit,       // activated, instead of LocalNavController
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.granted))
        Spacer(Modifier.height(8.dp))
        Button(onClick = onPermissionGranted) {
            Text(stringResource(R.string.continue_))
        }
    }
}

@Composable
fun NotGrantedRequestableBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.requestable))
        Spacer(Modifier.height(8.dp))
        Button(onClick = action) {
            Text(stringResource(R.string.allow_camera))
        }
    }
}

@Composable
fun NotGrantedShowRationaleBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.rationale))
        Spacer(Modifier.height(8.dp))
        Button(onClick = action) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@Composable
fun NotGrantedMaybePermanentlyDeniedBlock(
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.blocked))
        Spacer(Modifier.height(8.dp))
        Button(onClick = action ) {
            Text(stringResource(R.string.open_settings))
        }
    }
}

@Composable
fun PermissionScreenAndroid(
    // activated: the screen reports 'granted', the host decides where to go
    onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    // LocalActivity instead of 'context as Activity' (LocalContext may be a ContextWrapper)
    val activity = LocalActivity.current ?: return
    val permission = Manifest.permission.CAMERA

    // single flag we track ourselves
    // persisted (UiTools: wasPermissionRequested), was: rememberSaveable { false }
    var hasEverRequested by remember { mutableStateOf(wasPermissionRequested(context, permission)) }

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
        markPermissionRequested(context, permission)
        hasEverRequested = true
        uiState = computeUiState()
        // if (isGranted) onPermissionGranted()
    }

    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopCenter) {
        RoundedRectangle {
            when (uiState) {
                PermissionUiState.Granted -> {
                    GrantedBlock(onPermissionGranted)
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
    onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    // persisted and shared with PermissionScreenAndroid (same permission, same flag)
    var hasEverRequested by remember { mutableStateOf(wasPermissionRequested(context, Manifest.permission.CAMERA)) }
    // the callback runs when the dialog returns; status itself is observable state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) {
        markPermissionRequested(context, Manifest.permission.CAMERA)
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
                    GrantedBlock(onPermissionGranted)
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
