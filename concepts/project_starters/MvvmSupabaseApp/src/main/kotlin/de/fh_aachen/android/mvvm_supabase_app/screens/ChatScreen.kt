// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.data.ChatMessage
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.ChatViewModel

// Stateful: connects ChatViewModel and ChatScreen.
@Composable
fun ChatRoute(viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    ChatScreen(
        messages = messages,
        input = viewModel.input,
        error = viewModel.error,
        canSend = viewModel.canSend,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::send,
    )
}

// Stateless: messages (newest at the bottom), an error line, and the input row.
@Composable
fun ChatScreen(
    messages: List<ChatMessage>?,
    input: String,
    error: String?,
    canSend: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        if (error != null) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            when {
                messages == null -> Centered { CircularProgressIndicator() }
                messages.isEmpty() -> Centered { Text(stringResource(R.string.no_messages)) }
                // reverseLayout: the list is newest-first and starts at the bottom, like every chat
                else -> LazyColumn(
                    reverseLayout = true,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(messages, key = { it.id }) { message -> MessageBubble(message) }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text(stringResource(R.string.message_hint)) },
                maxLines = 4,
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(onClick = onSend, enabled = canSend) {
                Icon(painterResource(R.drawable.icon_send), contentDescription = stringResource(R.string.send))
            }
        }
    }
}

// One message: own ones on the right, others on the left with the author's name.
@Composable
private fun MessageBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.mine) Alignment.End else Alignment.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (message.mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!message.mine) {
                    Text(message.author, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(message.body, style = MaterialTheme.typography.bodyLarge)
                val time = formatTimestamp(message.createdAt)
                Text(
                    if (message.editedAt != null) "$time · ${stringResource(R.string.edited)}" else time,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    SupabaseTheme {
        ChatScreen(
            messages = listOf(
                ChatMessage(2, "Me", "Hi Ada!", "2026-10-31T12:01:00+00:00", mine = true),
                ChatMessage(1, "Ada", "Hello everybody", "2026-10-31T12:00:00+00:00"),
            ),
            input = "",
            error = null,
            canSend = false,
            onInputChange = {},
            onSend = {},
        )
    }
}
