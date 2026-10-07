// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import de.fh_aachen.android.location.R.drawable.background_castle
import de.fh_aachen.android.location.R.drawable.background_permission
import de.fh_aachen.android.location.R.drawable.background_sea
import de.fh_aachen.android.location.R.drawable.icon_home
import de.fh_aachen.android.location.R.drawable.icon_location
import de.fh_aachen.android.location.R.drawable.icon_permission
import de.fh_aachen.android.location.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

enum class Screen { Home, Permission, Location }

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
                        Screen.Location to NavScreen(icon_location, background_sea) { LocationScreen() },
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
            Text("Location - Login", fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
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
fun PermissionScreen() {
    val context = LocalContext.current
    val locationPermissionsState = rememberMultiplePermissionsState(locationPermissions)

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xeeff0000)).padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Permission Location", fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
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
                        Text("Request")
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

// precise or approximate - either one is enough for the map
fun isLocationPermissionGranted(context: Context) = locationPermissions.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val granted = remember { isLocationPermissionGranted(context) }

    // Navigation is a side effect: never call navigate() directly in the composable body.
    LaunchedEffect(granted) {
        if (!granted) navController.navigate(Screen.Permission.name)
    }
    // Without permission nothing below may run: the map's my-location layer would throw a SecurityException.
    if (!granted) return

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Start updates when the screen enters the composition, stop them when it leaves.
    // Without onDispose the updates would keep running (battery, leaked callback).
    DisposableEffect(fusedLocationClient) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
        startLocationUpdates(fusedLocationClient, callback)
        onDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    // rememberMarkerState(position = ...) only uses the position initially; so we keep one
    // MarkerState and move it whenever a new location arrives.
    val markerState = rememberMarkerState()
    LaunchedEffect(userLocation) {
        userLocation?.let { markerState.position = it }
    }

    // Display the map with the marker at the user's current location
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GoogleMap(
            modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.7f),
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            if (userLocation != null) {
                Marker(state = markerState, title = "Current Location")
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
