// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

@file:OptIn(ExperimentalStdlibApi::class)

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import de.fh_aachen.android.ui_tools.LocalSnackbarController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DialogScreen() {
    var openDialog by remember { mutableStateOf(false) }
    var openAlert by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    val snackbar = LocalSnackbarController.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = "Dialogs")
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openDialog = true; openAlert = false }) {
            Text("Coffee Dialog", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openAlert = true; openDialog = false }) {
            Text("Save Confirmation", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { scope.launch { snackbar.show("Saved") } }) {
            Text("SnackBar", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { showSaved = true }) {
            Text("Info", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }

    LaunchedEffect(showSaved) {
        if (showSaved) {
            delay(1000)
            showSaved = false
        }
    }

    AnimatedVisibility(
        visible = showSaved,
        enter = fadeIn(), exit = fadeOut(),
        modifier = Modifier.padding(8.dp)
    ) {
        RoundedRectangleWithText(text = "Saved")
    }

    if (openDialog) {
        ModernModalDialogNoBlur(
            onDismiss = { openDialog = false /*...*/ },
            onConfirm = { openDialog = false /*...*/ }
        )
    }

    if (openAlert) {
        ConfirmDeleteDialog(
            onDismiss = { openAlert = false },
            onConfirm = { openAlert = false }
        )
    }
}

@Composable
fun ModernModalDialogNoBlur(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.matchParentSize()
                    .background(Color.Black.copy(alpha = 0.32f))
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Quick question", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Grab a coffee?")
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Nope") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onConfirm) { Text("Do it") }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save item?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
