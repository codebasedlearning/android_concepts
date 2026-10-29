// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm.sensors

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor.TemperatureUnit
import de.fh_aachen.android.raw_sensor_mvvm.RawSensorMVVMApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemperatureViewModel(
    private val repository: SensorFlow,             // interface, not the concrete class -> a fake works in tests
    private val savedStateHandle: SavedStateHandle, // a test simply passes SavedStateHandle()
) : ViewModel() {

    /*
     * SavedStateHandle - what goes in, what stays out?
     *  Rule: save what the user did, reload what the app fetched.
     *   - unit:          a user choice -> handle. Survives rotation (the ViewModel survives anyway)
     *                    *and* process death (the handle is part of the saved instance state).
     *   - temperature:   sensor data -> not saved, the repository delivers it again.
     *   - isCalibrating: not saved either! The calibration runs in viewModelScope; after process
     *                    death that coroutine is gone. A restored 'true' would show a spinner
     *                    for an operation nobody runs any more - forever.
     *
     *  The handle is the single source of truth for 'unit': reads via getStateFlow, writes via
     *  savedStateHandle[KEY] = …, no extra MutableStateFlow next to it (two sources of truth).
     *  It is snapshotted in onSaveInstanceState, i.e. when the app goes to the background.
     *
     *  Try it: switch to °F, send the app to the background, 'adb shell am kill
     *  de.fh_aachen.android.raw_sensor_mvvm', reopen it from Recents. Still °F.
     */
    val unit: StateFlow<TemperatureUnit> = savedStateHandle.getStateFlow(KEY_UNIT, TemperatureUnit.Celsius)

    fun toggleUnit() {
        savedStateHandle[KEY_UNIT] = unit.value.toggled()
    }

    // The sensor delivers °C, the UI wants the chosen unit, so we combine both flows (before:
    // map over the sensor only). After combine we have a simple flow and .stateIn transfers it
    // back to a StateFlow, so we are required to say where to store the value and with which
    // initial value. 'unit' must be declared above, it is used during initialization.
    //
    // SharingStarted.WhileSubscribed(5_000): the ViewModel collects only while the UI collects
    //  (plus 5s grace, so a rotation does not restart the sensor). With Eagerly, the ViewModel
    //  kept the sensor running in the background although collectAsStateWithLifecycle had
    //  already stopped listening.
    // For comparison
    // - Eagerly: starts immediately and never stops (until the scope dies). The upstream runs
    //   in the background.
    // - Lazily: starts with the first subscriber and never stops. Okay for cheap or one-shot work,
    //   bad for sensors.
    // - WhileSubscribed: follows demand. That’s why it’s the default recommendation for UI state,
    //   and why the 5s are the officially blessed magic number: longer than any config change,
    //   shorter than a user’s patience.
    val temperature: StateFlow<Int> = combine(repository.data, unit) { data, unit -> unit.fromCelsius(data.rawValue) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), unit.value.fromCelsius(repository.data.value.rawValue))

    // Classic MVVM style: one more observable property per piece of UI state. Compare with MVI,
    //  where this is one field in one state object - with three or four of these (now we have
    //  three), you see why people start bundling them.
    //
    //  Explicit backing field (stable since Kotlin 2.4, experimental in 2.3): outside, the
    //  property is a read-only StateFlow; inside this class, the compiler smart casts it to the
    //  MutableStateFlow in 'field', so we write 'isCalibrating.value = …' directly.
    //  It replaces the classic backing-property pattern
    //      private val _isCalibrating = MutableStateFlow(false)
    //      val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()
    //  Trade-off ('downcast trap'): the field syntax only *hides* the type, the object handed out
    //  is still the MutableStateFlow, so '(vm.isCalibrating as MutableStateFlow).value = true'
    //  compiles and works. asStateFlow() returns a separate read-only wrapper, the same cast
    //  throws. The field protects against mistakes, asStateFlow() also against deliberate casts.
    //  A cast like that is a code-review problem, not a type-system problem - so: less code.
    val isCalibrating: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun calibrate() {
        // Ignore double taps while a calibration runs. No race: viewModelScope runs on
        //  Dispatchers.Main.immediate, so the launch below sets the flag synchronously, before
        //  a second tap can be handled.
        if (isCalibrating.value) return

        // repository.calibrate() is suspend. The ViewModel owns the coroutine
        //  (viewModelScope), so it is cancelled automatically when the screen is gone for good.
        viewModelScope.launch {
            isCalibrating.value = true
            try {
                repository.calibrate()
            } finally {
                isCalibrating.value = false     // also on failure or cancellation
            }
        }
    }

    companion object {
        private const val KEY_UNIT = "unit"

        // The factory is the only place that knows the Application (and thus the container).
        //  this[APPLICATION_KEY] returns Application?, the cast fails loudly if the manifest
        //  names another Application class.
        //  createSavedStateHandle() takes the owner's SavedStateRegistry from the CreationExtras
        //  that viewModel() passes in, restores what was saved for this ViewModel (after process
        //  death) and registers the handle for the next snapshot. Without a NavHost the owner
        //  is the Activity; inside a NavHost it would be the NavBackStackEntry.
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RawSensorMVVMApplication
                TemperatureViewModel(
                    repository = app.container.temperatureRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
