// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo1.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import de.fh_aachen.android.photo1.model.PhotoViewModel
import de.fh_aachen.android.photo1.model.createImageFileInAppStorage
import de.fh_aachen.android.photo1.model.createImageUriInMediaStore
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlin.toString

@Composable
fun CameraScreen() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(12.dp))
        RoundedRectangleWithText(text = "Photo 1 - Camera")
        PhotoScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    viewModel: PhotoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe the last captured photo
    val photoUri by viewModel.photoUri.collectAsState()

    // We keep the URI where the camera should write the photo
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher using TakePicture – it writes into a pre-created URI
    val takePictureLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && captureUri != null) {
                viewModel.onPhotoCaptured(captureUri!!)
            }
        }

    RoundedRectangle(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    val file = createImageFileInAppStorage(context) // Variant A: save into app-specific file
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    captureUri = uri
                    takePictureLauncher.launch(uri)
                }) { Text("Capture to file") }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = {
                    val uri = createImageUriInMediaStore(context) // Variant B: save into Photos / MediaStore
                    if (uri != null) {
                        captureUri = uri
                        takePictureLauncher.launch(uri)
                    }
                }) { Text("Capture to Photos") }
            }

            if (photoUri != null) {
                Text("Last photo URI:")
                Text(photoUri.toString(), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("No photo captured yet.")
            }
        }
    }
}
