// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor_mvvm.RawSensorMVVMApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemperatureViewModel(
    private val repository: SensorFlow      // interface, not the concrete class -> a fake works in tests
) : ViewModel() {

    // We need to extract single dimensions out of the flow, that is what map is for, but
    // after map we have a simple flow and .stateIn transfers it back to a StateFlow,
    // so we are required to say where to store the value and with which initial value.
    //
    // SharingStarted.Eagerly -> WhileSubscribed(5_000). The ViewModel now collects only
    //  while the UI collects (plus 5s grace, so a rotation does not restart the sensor). With
    //  Eagerly, the ViewModel kept the sensor running in the background although
    //  collectAsStateWithLifecycle had already stopped listening.
    val temperature: StateFlow<Int> = repository.data.map { it.rawValue }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.data.value.rawValue)

    // New, so that both apps have the same features. Classic MVVM style: one more
    //  observable property per piece of UI state. Compare with MVI, where this is one field in
    //  one state object - with three or four of these, you see why people start bundling them.
    //
    //  '_x' + asStateFlow() instead of an explicit backing field ('field = MutableStateFlow(..)'):
    //  asStateFlow() returns a read-only wrapper, so nobody outside can cast it back to
    //  MutableStateFlow and write to it. The explicit backing field only hides the type.
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    fun calibrate() {
        // Ignore double taps while a calibration runs. No race: viewModelScope runs on
        //  Dispatchers.Main.immediate, so the launch below sets the flag synchronously, before
        //  a second tap can be handled.
        if (_isCalibrating.value) return

        // repository.calibrate() is suspend now. The ViewModel owns the coroutine
        //  (viewModelScope), so it is cancelled automatically when the screen is gone for good.
        viewModelScope.launch {
            _isCalibrating.value = true
            try {
                repository.calibrate()
            } finally {
                _isCalibrating.value = false    // also on failure or cancellation
            }
        }
    }

    // this[APPLICATION_KEY] returns Application?
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RawSensorMVVMApplication
                TemperatureViewModel(app.temperatureRepository)
            }
        }
    }
}
