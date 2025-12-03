// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm.sensors

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.raw_sensor_mvvm.RawSensorMVVMApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAG = "SENSOR"


// general sensor structures

data class SensorData(val rawValue: Int)        // could contain more values

interface SensorFlow {
    val data: StateFlow<SensorData>
    fun calibrate()                             // dummy functionality
}

// random values
fun ClosedRange<Int>.center() = start + (endInclusive - start) / 2
fun Int.nextJitter(withinRange: ClosedRange<Int>) = (this + (-2..2).random()).coerceIn(withinRange)


// specific temperature sensor configurations

class TemperatureRepository(private val simulationScope: CoroutineScope): SensorFlow {
    private val _sensorTemperatureRange: ClosedRange<Int> = -5..15
    private val _data = MutableStateFlow(SensorData(rawValue = _sensorTemperatureRange.center()))
    override val data: StateFlow<SensorData> get() = _data

    init {
        startUpdating()
    }

    private fun startUpdating() {
        simulationScope.launch {
            while (true) {
                _data.value = SensorData(rawValue = _data.value.rawValue.nextJitter(withinRange = _sensorTemperatureRange))
                Log.i(TAG,"new temperature value: ${_data.value.rawValue}")
                delay(1000L)
            }
        }
    }

    override fun calibrate() {
        Log.i(TAG,"calibrate device")
    }
}

class TemperatureViewModel : ViewModel() {
    private val repository = RawSensorMVVMApplication.serviceLocator.temperatureRepository

    val data: StateFlow<SensorData> = repository.data

    // We need to extract single dimensions out of the flow, that is what map is for, but
    // after map we have a simple flow and .stateIn transfers it back to a StateFlow,
    // so we are required to say where to store the value and with which initial value.

    val temperature: StateFlow<Int> = data.map { it.rawValue }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.data.value.rawValue)

    fun calibrate() {
        repository.calibrate()
    }
}
