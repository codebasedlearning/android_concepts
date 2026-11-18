// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.fh_aachen.android.modal_dialog.R
import de.fh_aachen.android.ui_tools.LocalSnackbarHostState
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Which dialog is open - one value instead of two booleans ('openDialog', 'openAlert').
 *  With two flags, "both open" was a possible state that every button had to rule out by hand
 *  ('openDialog = true; openAlert = false'). With one value it cannot even be written down.
 */
enum class OpenDialog { None, Coffee, SaveConfirm }

@Composable
fun DialogScreen() {
    /*
     * rememberSaveable instead of remember: an open dialog stays open after a rotation, and
     *  (thanks to the SaveableStateHolder entry decorator in MainActivity) also after a tab switch.
     *  An enum can be saved as it is (it is Serializable).
     */
    var openDialog by rememberSaveable { mutableStateOf(OpenDialog.None) }
    var showInfo by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val savedText = stringResource(R.string.saved)
    val coffeeText = stringResource(R.string.coffee_confirmed)

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = stringResource(R.string.dialogs))
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openDialog = OpenDialog.Coffee }) {
            Text(stringResource(R.string.coffee_dialog), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openDialog = OpenDialog.SaveConfirm }) {
            Text(stringResource(R.string.save_confirmation), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { showInfo = true }) {
            Text(stringResource(R.string.info), fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }

    // Not modal at all, for comparison: a hint that shows up and disappears on its own.
    LaunchedEffect(showInfo) {
        if (showInfo) {
            delay(1000)
            showInfo = false
        }
    }

    // Note: Column, AnimatedVisibility and the dialogs below are siblings - the parent (a Box from
    // ScreenWithBackground) stacks them, so the info appears on top of the column, at the top start.
    AnimatedVisibility(
        visible = showInfo,
        enter = fadeIn(), exit = fadeOut(),
        modifier = Modifier.padding(8.dp)
    ) {
        RoundedRectangleWithText(text = stringResource(R.string.info_text))
    }

    // Feedback after the confirmed action is the natural place for the snackbar; dismissing
    // ('Nope', 'Cancel', tap outside, Back) needs none - nothing happened.
    //
    // What happens?
    // The click handler sets openDialog = OpenDialog.Coffee. openDialog is a snapshot state
    // (mutableStateOf). Compose remembers who read this state during the last composition:
    // DialogScreen itself, in the when.
    // Compose marks that scope as invalid and re-executes DialogScreen() on the next frame.
    // The values from remember/rememberSaveable come back from Compose’s memory (the slot table);
    // they are not re-initialized.
    // The when now goes into the Coffee branch. ModernModalDialog(...) is called, so it is now part
    // of the composition, and Dialog opens its window.
    // The unchanged parts are mostly skipped. The Column with the buttons gets the same parameters
    // as before, so Compose doesn’t redo its work.
    //
    // Closing works the other way round (None).
    when (openDialog) {
        OpenDialog.None -> {}
        OpenDialog.Coffee -> ModernModalDialog(
            onDismiss = { openDialog = OpenDialog.None },
            onConfirm = {
                openDialog = OpenDialog.None
                scope.launch { snackbarHostState.showSnackbar(coffeeText) }
            }
        )
        OpenDialog.SaveConfirm -> ConfirmSaveDialog(
            onDismiss = { openDialog = OpenDialog.None },
            onConfirm = {
                openDialog = OpenDialog.None
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
