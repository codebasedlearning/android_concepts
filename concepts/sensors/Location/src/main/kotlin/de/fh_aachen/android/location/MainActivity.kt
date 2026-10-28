// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.location

import androidx.compose.ui.res.stringResource
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.fh_aachen.android.location.R.drawable.background_castle
import de.fh_aachen.android.location.R.drawable.background_permission
import de.fh_aachen.android.location.R.drawable.background_sea
import de.fh_aachen.android.location.R.drawable.icon_home
import de.fh_aachen.android.location.R.drawable.icon_location
import de.fh_aachen.android.location.R.drawable.icon_permission
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

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object Permission : Screen
    @Serializable data object Location : Screen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                LocationApp()
            }
        }
    }
}

/*
 *   No automatic redirect to the permission screen any more (it was a LaunchedEffect
 *   that navigated as soon as the permission was missing, and Back could loop). 'Permission
 *   missing' is state: the location screen shows a hint with a button, the user decides. And the
 *   permission screen reports 'granted', so the host can lead back to the map.
 */
@Composable
fun LocationApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Permission, icon_permission, stringResource(R.string.nav_permission)),
            ConceptDestination(Screen.Location, icon_location, stringResource(R.string.nav_location)),
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
                        PermissionScreen(onPermissionGranted = { backStack.navigateToTopLevel(Screen.Location) })
                    }
                }
                entry<Screen.Location> {
                    ScreenWithBackground(background_sea) {
                        LocationScreen(onOpenPermission = { backStack.navigateToTopLevel(Screen.Permission) })
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

/*
 * Since Android 12 you request FINE and COARSE location together; the system dialog then lets
 * the user choose between 'precise' and 'approximate'. Requesting FINE alone is ignored on
 * Android 12+. So we ask for both and accept either result.
 */
private val locationPermissions = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

// see the Camera app for the single-permission variant
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {     // reports 'granted' to the host
    val context = LocalContext.current
    val locationPermissionsState = rememberMultiplePermissionsState(locationPermissions)

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xeeff0000)).padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(stringResource(R.string.permission_title), fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    locationPermissionsState.permissions.forEach { state ->
                        val name = state.permission.substringAfterLast('.')
                        Text("$name: granted ${state.status.isGranted}, rationale ${state.status.shouldShowRationale}",
                            fontSize = 18.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xccff8000)).padding(2.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(onClick = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }) {
                        Text(stringResource(R.string.request))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { gotoSettingsActivity(context) }) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
            // new: once granted (precise or approximate), offer the way to the map
            if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPermissionGranted) {
                    Text(stringResource(R.string.continue_to_map))
                }
            }
        }
    }
}

// precise or approximate - either one is enough for the map
fun isLocationPermissionGranted(context: Context) = locationPermissions.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun LocationScreen(onOpenPermission: () -> Unit) {
    val context = LocalContext.current

    // Re-checked on every resume (e.g. back from the system settings). Before, it was
    //  checked once, and a missing permission triggered a navigation from a LaunchedEffect.
    var granted by remember { mutableStateOf(isLocationPermissionGranted(context)) }
    LifecycleResumeEffect(Unit) {
        granted = isLocationPermissionGranted(context)
        onPauseOrDispose { }
    }

    // Without permission nothing below may run: the map's my-location layer would throw a SecurityException.
    if (!granted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangle {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.permission_missing))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenPermission) { Text(stringResource(R.string.grant_permission)) }
                }
            }
        }
        return
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    /*
     *  LifecycleStartEffect instead of DisposableEffect: updates run between onStart and
     *  onStop. DisposableEffect only stopped them when the screen left the composition - with the
     *  app in the background (screen still composed) GPS kept running and draining the battery.
     *  Composition-aware vs. lifecycle-aware, see the comment in the Permissions app.
     */
    LifecycleStartEffect(fusedLocationClient) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
        startLocationUpdates(fusedLocationClient, callback)
        onStopOrDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    // rememberMarkerState(position = ...) only uses the position initially; so we keep one
    // MarkerState and move it whenever a new location arrives.
    val markerState = rememberMarkerState()

    /*
     *  The map's camera. Without it the map shows the whole world (lat/lng 0/0, zoom 0)
     *  and the marker is a tiny dot somewhere. We move the camera to the first fix only, so the
     *  user can pan and zoom afterwards without being pulled back every few seconds.
     */
    val cameraPositionState = rememberCameraPositionState()
    var centeredOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        val location = userLocation ?: return@LaunchedEffect
        markerState.position = location
        if (!centeredOnce) {
            centeredOnce = true
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    // Display the map with the marker at the user's current location
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GoogleMap(
            modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.7f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            if (userLocation != null) {
                Marker(state = markerState, title = stringResource(R.string.current_location))
            }
        }
    }
}

// not a @Composable; the caller (LocationScreen) has checked the permission
@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    callback: LocationCallback,
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(2000)
        .setMaxUpdateDelayMillis(10000)
        .setWaitForAccurateLocation(true)
        .build()

    fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
}
