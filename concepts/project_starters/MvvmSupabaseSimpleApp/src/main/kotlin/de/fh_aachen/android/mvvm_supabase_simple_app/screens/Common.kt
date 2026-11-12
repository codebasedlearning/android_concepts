// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.mvvm_supabase_simple_app.R
import de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels.Load

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
