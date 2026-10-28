// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.ui.res.stringResource
import de.fh_aachen.android.modal_dialog.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PickerScreen() {
    var openDate by remember { mutableStateOf(false) }
    var openTime by remember { mutableStateOf(false) }
    var openColor by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    // the pickers' results are kept and shown
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = stringResource(R.string.pickers))
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openDate = true }) {
            Text(stringResource(R.string.date_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openTime = true }) {
            Text(stringResource(R.string.time_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        if (selectedDate != null || selectedTime != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val dateText = selectedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "–"
            RoundedRectangleWithText(text = "$dateText, ${selectedTime ?: "–"}")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = selectedColor,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(80.dp)
        ) {}
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openColor = true }) {
            Text(stringResource(R.string.color_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }

    if (openDate) {
        DatePickerSample(
            onDismiss = { openDate = false },
            onDateSelected = { millis ->
                /*
                 * Careful with time zones: the DatePicker returns midnight UTC of the chosen
                 * day. Converted with the device's zone, everybody west of Greenwich would see the
                 * day before (midnight UTC is still 'yesterday' in New York). So: read it as UTC.
                 */
                selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                openDate = false
            }
        )
    }

    if (openTime) {
        TimePickerSample(
            onDismiss = { openTime = false },
            onTimeSelected = { hour, minute ->
                selectedTime = "%02d:%02d".format(hour, minute)
                openTime = false
            }
        )
    }

    if (openColor) {
        ColorPickerBottomSheet(
            initialColor = selectedColor,
            onDismiss = { openColor = false },
            onColorSelected = {
                selectedColor = it
                openColor = false
            }
        )
    }
}

@Composable
fun DatePickerSample(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let(onDateSelected)
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun TimePickerSample(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState()

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(state.hour, state.minute)
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.select_time)) }
    ) {
        TimePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
        Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
        Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
        Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800),
        Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B)
    )

    /*
     * Close the sheet with its slide-down animation. Before, the caller simply removed it
     * from the composition (openColor = false), so it vanished abruptly. The Material pattern:
     * animate hide() first (a suspend function), and remove the sheet when that is done.
     * Swipe-down and tap-outside go through onDismissRequest, where the sheet is already hidden.
     */
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.choose_color),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colors) { color ->
                    ColorSwatch(
                        color = color,
                        selected = color == initialColor,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) onColorSelected(color)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Surface(onClick = ...) instead of Modifier.clickable: the ripple is clipped to the
    // shape and the semantics (role, enabled) are set correctly.
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        tonalElevation = if (selected) 6.dp else 0.dp,
        shadowElevation = if (selected) 8.dp else 2.dp,
        modifier = Modifier.size(44.dp),
        border = if (selected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
        else null
    ) {}
}

