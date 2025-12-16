// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText

@Composable
fun PickerScreen() {
    var openDate by remember { mutableStateOf(false) }
    var openTime by remember { mutableStateOf(false) }
    var openColor by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = "Pickers")
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openDate = true }) {
            Text("Date Picker", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openTime = true }) {
            Text("Time Picker", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
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
            Text("Color Picker", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }

    if (openDate) {
        DatePickerSample(
            onDismiss = { openDate = false },
            onDateSelected = { openDate = false }
        )
    }

    if (openTime) {
        TimePickerSample(
            onDismiss = { openTime = false },
            onTimeSelected = { _, _ -> openTime = false }
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
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("time") }
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

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "Choose color",
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
                        onClick = { onColorSelected(color) }
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
    Surface(
        shape = CircleShape,
        color = color,
        tonalElevation = if (selected) 6.dp else 0.dp,
        shadowElevation = if (selected) 8.dp else 2.dp,
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        border = if (selected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
        else null
    ) {}
}

