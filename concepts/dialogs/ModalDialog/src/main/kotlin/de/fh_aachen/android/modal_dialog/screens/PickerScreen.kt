// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.modal_dialog.R
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// The colors of the bottom sheet; the initial color is one of them, so it shows up as selected.
private val Palette = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
    Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
    Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800),
    Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B)
)

// Which picker is open - one value instead of three booleans, cf. OpenDialog.
enum class OpenPicker { None, Date, Time, Color }

@Composable
fun PickerScreen() {
    /*
     * rememberSaveable keeps the results across a rotation and a tab switch. It can only save
     *  what fits into a Bundle, so the state holds simple values and the screen derives the rest:
     *      Color      -> Int (ARGB),
     *      LocalDate  -> Long (epoch day, days since 1970-01-01),
     *      LocalTime  -> Int (minute of the day).
     *  Alternative: a custom Saver for each type, or a ViewModel with SavedStateHandle.
     */
    var openPicker by rememberSaveable { mutableStateOf(OpenPicker.None) }
    var colorArgb by rememberSaveable { mutableIntStateOf(Palette[5].toArgb()) }
    var epochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var minuteOfDay by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectedColor = Color(colorArgb)
    val selectedDate = epochDay?.let(LocalDate::ofEpochDay)
    val selectedTime = minuteOfDay?.let { LocalTime.of(it / 60, it % 60) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = stringResource(R.string.pickers))
        Spacer(modifier = Modifier.height(36.dp))

        Button(onClick = { openPicker = OpenPicker.Date }) {
            Text(stringResource(R.string.date_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { openPicker = OpenPicker.Time }) {
            Text(stringResource(R.string.time_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        if (selectedDate != null || selectedTime != null) {
            Spacer(modifier = Modifier.height(12.dp))
            // both in the device's format (e.g. "18.11.2026, 19:30" or "Nov 18, 2026, 7:30 PM")
            val dateText = selectedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "–"
            val timeText = selectedTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: "–"
            RoundedRectangleWithText(text = "$dateText, $timeText")
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
        Button(onClick = { openPicker = OpenPicker.Color }) {
            Text(stringResource(R.string.color_picker), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }

    // State down: every picker starts with the current value. Events up: the result comes back.
    when (openPicker) {
        OpenPicker.None -> {}
        OpenPicker.Date -> DatePickerSample(
            initialDate = selectedDate,
            onDismiss = { openPicker = OpenPicker.None },
            onDateSelected = { date ->
                epochDay = date.toEpochDay()
                openPicker = OpenPicker.None
            }
        )
        OpenPicker.Time -> TimePickerSample(
            initialTime = selectedTime,
            onDismiss = { openPicker = OpenPicker.None },
            onTimeSelected = { time ->
                minuteOfDay = time.hour * 60 + time.minute
                openPicker = OpenPicker.None
            }
        )
        OpenPicker.Color -> ColorPickerBottomSheet(
            initialColor = selectedColor,
            onDismiss = { openPicker = OpenPicker.None },
            onColorSelected = {
                colorArgb = it.toArgb()
                openPicker = OpenPicker.None
            }
        )
    }
}

/*
 * Careful with time zones: the DatePicker works with midnight UTC of the chosen day, in both
 *  directions. Converted with the device's zone, everybody west of Greenwich would see the day
 *  before (midnight UTC is still 'yesterday' in New York). So: convert with UTC, here and only here -
 *  outside, the date is a LocalDate, without any time or zone.
 */
@Composable
fun DatePickerSample(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // no date selected yet -> nothing to confirm
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

// Starts with the last chosen time, or now. 12 or 24 hours follows the device setting
// (the default of rememberTimePickerState), and so does the display on the screen.
@Composable
fun TimePickerSample(
    initialTime: LocalTime?,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    val start = initialTime ?: LocalTime.now()
    val state = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute)

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(state.hour, state.minute))
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
    /*
     * Close the sheet with its slide-down animation. Before, the caller simply removed it
     * from the composition (openPicker = None), so it vanished abruptly. The Material pattern:
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
                items(Palette) { color ->
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
