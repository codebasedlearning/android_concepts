// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.supabase.R
import de.fh_aachen.android.supabase.model.Message
import de.fh_aachen.android.supabase.model.RealtimeViewModel
import de.fh_aachen.android.ui_tools.ConceptTheme

@Composable
fun RealtimeRoute(viewModel: RealtimeViewModel = viewModel(factory = RealtimeViewModel.Factory)) {
    val message by viewModel.lastMessage.collectAsStateWithLifecycle()
    val sent by viewModel.sent.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    RealtimeScreen(message = message, sent = sent, error = error, onPing = viewModel::ping)
}

@Composable
fun RealtimeScreen(message: Message?, sent: Int, error: String?, onPing: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.realtime_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.realtime_hint), style = MaterialTheme.typography.bodySmall)
                Text(
                    message?.msg ?: stringResource(R.string.no_message_yet),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                message?.let { Text(stringResource(R.string.message_no, it.no)) }
                Button(onClick = onPing) { Text(stringResource(R.string.send_ping)) }
                Text(stringResource(R.string.sent_count, sent), style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Preview
@Composable
private fun RealtimeScreenPreview() {
    ConceptTheme {
        RealtimeScreen(message = Message("HuHu Android", 3), sent = 2, error = null, onPing = {})
    }
}
