// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.ui.res.stringResource
import de.fh_aachen.android.modal_dialog.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import de.fh_aachen.android.ui_tools.LocalSnackbarHostState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DialogScreen() {
    var openDialog by remember { mutableStateOf(false) }
    var openAlert by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val savedText = stringResource(R.string.saved)

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = stringResource(R.string.dialogs))
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openDialog = true; openAlert = false }) {
            Text(stringResource(R.string.coffee_dialog), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openAlert = true; openDialog = false }) {
            Text(stringResource(R.string.save_confirmation), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { showSaved = true }) {
            Text(stringResource(R.string.info), fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }

    LaunchedEffect(showSaved) {
        if (showSaved) {
            delay(1000)
            showSaved = false
        }
    }

    // Note: Column, AnimatedVisibility and the dialogs below are siblings - the parent (a Box from
    // ScreenWithBackground) stacks them, so 'Saved' appears on top of the column, at the top start.
    AnimatedVisibility(
        visible = showSaved,
        enter = fadeIn(), exit = fadeOut(),
        modifier = Modifier.padding(8.dp)
    ) {
        RoundedRectangleWithText(text = savedText)
    }

    if (openDialog) {
        ModernModalDialog(
            onDismiss = { openDialog = false /*...*/ },
            onConfirm = { openDialog = false /*...*/ }
        )
    }

    if (openAlert) {
        ConfirmSaveDialog(
            onDismiss = { openAlert = false },
            onConfirm = {
                openAlert = false
                // the natural place for the snackbar: feedback after the confirmed action
                // (before, a separate 'SnackBar' button showed it out of context)
                scope.launch { snackbarHostState.showSnackbar(savedText) }
            }
        )
    }
}

@Composable
fun ModernModalDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(stringResource(R.string.quick_question), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.grab_a_coffee))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.nope)) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm) { Text(stringResource(R.string.do_it)) }
                }
            }
        }
    }
}

@Composable
fun ConfirmSaveDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_item_title)) },
        text = { Text(stringResource(R.string.save_item_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
