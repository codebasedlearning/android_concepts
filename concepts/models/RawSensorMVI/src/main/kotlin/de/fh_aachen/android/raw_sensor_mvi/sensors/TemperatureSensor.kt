// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi.sensors

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.raw_sensor_mvi.RawSensorMVIApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * Key things that changed relative to MVVM:
 *  – UI no longer calls methods like viewModel.calibrate() directly.
 *  – UI instead sends intents via dispatch(intent).
 *  – ViewModel exposes immutable state via StateFlow<TemperatureViewState>.
 *  – Sensor updates are collected and reduced into new state
 */

const val TAG = "SENSOR"

// ----------------------------
// General sensor structures
// ----------------------------

data class SensorData(val rawValue: Int)        // could contain more values

interface SensorFlow {
    val data: StateFlow<SensorData>
    fun calibrate()                             // dummy functionality
}

// random values
fun ClosedRange<Int>.center() = start + (endInclusive - start) / 2

fun Int.nextJitter(withinRange: ClosedRange<Int>): Int =
    (this + (-2..2).random()).coerceIn(withinRange)

// ----------------------------
// Temperature repository (same as before, MVVM-agnostic)
// ----------------------------

class TemperatureRepository(private val simulationScope: CoroutineScope) : SensorFlow {
    private val _sensorTemperatureRange: ClosedRange<Int> = -5..15

    private val _data = MutableStateFlow(SensorData(rawValue = _sensorTemperatureRange.center()))
    override val data: StateFlow<SensorData> get() = _data

    init {
        startUpdating()
    }

    private fun startUpdating() {
        simulationScope.launch {
            while (true) {
                _data.value = SensorData(
                    rawValue = _data.value.rawValue.nextJitter(withinRange = _sensorTemperatureRange)
                )
                Log.i(TAG, "new temperature value: ${_data.value.rawValue}")
                delay(1000L)
            }
        }
    }

    override fun calibrate() {
        Log.i(TAG, "calibrate device")
        // here you could shift/normalize values etc.
    }
}

// ----------------------------
// MVI: State + Intent
// ----------------------------

data class TemperatureViewState(
    val temperature: Int = 0,
    val isCalibrating: Boolean = false,
    // more fields like
    // val error: String? = null
)

/**
 * Intents = everything the View "wants to do".
 * User actions only here; sensor updates are handled internally.
 */
sealed interface TemperatureIntent {
    object CalibrateClicked : TemperatureIntent

    // data class SetThreshold(val value: Int) : TemperatureIntent
    // usage: TemperatureIntent.SetThreshold(42)
}

/*
 * Why use a sealed interface for global objects like intents?
 *
 * A sealed interface means:
 *  – all possible intents must be known at compile time
 *  – they must live in the same file
 *  – the compiler will warn you if your when() is missing a case.
 *
 * An interface lets you mix:
 *  – objects (stateless 'events')
 *  – data classes (intents with payloads)
 *
 * Read it as a closed, enum-like union of intent variants.
 */

// ----------------------------
// MVI ViewModel ("store")
// ----------------------------

class TemperatureMviViewModel : ViewModel() {

    private val repository: TemperatureRepository = RawSensorMVIApplication.serviceLocator.temperatureRepository

    private val _state = MutableStateFlow(TemperatureViewState())
    val state: StateFlow<TemperatureViewState> = _state.asStateFlow()

    init {
        // Collect sensor data and reduce into state
        viewModelScope.launch {
            repository.data.collect { sensorData ->
                // Internal "action" -> state reduction
                reduceSensorData(sensorData)
            }
        }
    }

    fun dispatch(intent: TemperatureIntent) {
        when (intent) {
            TemperatureIntent.CalibrateClicked -> {
                repository.calibrate()
                // Internal "action" -> state reduction
                reduceCalibrate()
            }
        }
    }

    private fun reduceSensorData(data: SensorData) {
        _state.value = _state.value.copy(
            temperature = data.rawValue,
            isCalibrating = false,  // when we get new values, we are done
        )
    }

    private fun reduceCalibrate() {
        _state.value = _state.value.copy(
            isCalibrating = true,
        )
    }
}
