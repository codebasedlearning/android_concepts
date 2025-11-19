// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.location

import android.Manifest
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
import de.fh_aachen.android.location.R.drawable.icon_home
import de.fh_aachen.android.location.R.drawable.icon_permission
import de.fh_aachen.android.location.R.drawable.icon_location
import de.fh_aachen.android.location.R.drawable.background_castle
import de.fh_aachen.android.location.R.drawable.background_permission
import de.fh_aachen.android.location.R.drawable.background_sea
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

// see B_Camera

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen() {
    val context = LocalContext.current
    // V2
    val cameraPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xeeff0000)).padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Permission Location", fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status V2, granted: ${cameraPermissionState.status.isGranted}", fontSize = 24.sp, color = Color.White)
                    Text("Status V2, rationale: ${cameraPermissionState.status.shouldShowRationale}", fontSize = 24.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xccff8000)).padding(2.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
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

fun isLocationPermissionGranted(context: Context)
        = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen() {
    val context = LocalContext.current
    if (!isLocationPermissionGranted(context)) {
        val navController = LocalNavController.current
        navController.navigate(Screen.Permission.name)
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocation = remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(Unit) {
        startLocationUpdatesIfPermitted(fusedLocationClient, userLocation, context)
    }

    // Display the map with the marker at the user's current location
    Box(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.Center)
            .size(width = LocalConfiguration.current.screenWidthDp.dp * 0.8f,
                height = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            userLocation.value?.let { location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    title = "Current Location"
                )
            }
        }
    }
}

// not a @Composable
private fun startLocationUpdatesIfPermitted(
    fusedLocationClient: FusedLocationProviderClient,
    userLocation: MutableState<LatLng?>,
    context: Context
) {
    if (isLocationPermissionGranted(context)) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(2000)
            setMaxUpdateDelayMillis(10000)
            setWaitForAccurateLocation(true)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    userLocation.value = location?.let {
                        LatLng(it.latitude, it.longitude)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }
}
