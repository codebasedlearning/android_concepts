// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.Load
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Spinner, error with retry, or the content for the loaded data.
@Composable
fun <T> LoadContent(
    state: Load<T>,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        Load.Loading -> Centered { CircularProgressIndicator() }
        is Load.Error -> Centered {
            Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        is Load.Success -> content(state.data)
    }
}

// A full-size, centered column.
@Composable
fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}

// An image from the storage bucket. Loaded from the signed URL, but cached under the object
// path: the URL's token changes with every request, the path (it contains a UUID) never does.
@Composable
fun StorageImage(
    url: String,
    objectPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalPlatformContext.current
    val request = remember(url, objectPath) {
        ImageRequest.Builder(context)
            .data(url)
            .diskCacheKey(objectPath)
            .memoryCacheKey(objectPath)
            .build()
    }
    AsyncImage(model = request, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
}

// "2026-10-31T12:34:56.789+00:00" -> "31.10. 13:34" in the device's time zone.
fun formatTimestamp(iso: String): String = runCatching {
    OffsetDateTime.parse(iso)
        .atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))
}.getOrDefault(iso)
