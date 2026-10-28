// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.raw_sensor.SensorData
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor_mvi.RawSensorMVIApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Key things that changed relative to MVVM:
 *  – UI no longer calls methods like viewModel.calibrate() directly.
 *  – UI instead sends intents via dispatch(intent).
 *  – ViewModel exposes immutable state via StateFlow<TemperatureViewState>.
 *  – Sensor updates are collected and reduced into new state
 */

// ----------------------------
// MVI: State + Intent + Msg
// ----------------------------

data class TemperatureViewState(
    // No default '= 0' any more. 0 °C is a valid temperature, so it was a bad
    //  'not loaded yet' marker; the ViewModel seeds the state from the repository instead.
    val temperature: Int,
    val isCalibrating: Boolean = false,
    // more fields like
    // val error: String? = null
)

/**
 * Intents = everything the View "wants to do".
 * User actions only here; sensor updates are handled internally.
 */
sealed interface TemperatureIntent {
    data object CalibrateClicked : TemperatureIntent      // 'data object': readable toString()

    // data class SetThreshold(val value: Int) : TemperatureIntent
    // usage: TemperatureIntent.SetThreshold(42)
}

/*
 * Why use a sealed interface for global objects like intents?
 *
 * A sealed interface means:
 *  – all possible intents must be known at compile time
 *  – they must live in the same package and module (since Kotlin 1.5; before: same file)
 *  – a when() over them must be exhaustive, a missing case is a compile error (no 'else' needed).
 *
 * An interface lets you mix:
 *  – objects (stateless 'events')
 *  – data classes (intents with payloads)
 *
 * Read it as a closed, enum-like union of intent variants.
 */

/*
 * Intent vs. Msg - two sealed types answering two different questions:
 *  – TemperatureIntent: what does the user want?        (public, sent by the UI)
 *  – Msg:               what has happened to the state?  (internal, consumed by the reducer)
 * One intent can produce several messages (CalibrateClicked → Started … Finished), and messages
 * also come from elsewhere (the sensor). The reducer only knows messages, so every single state
 * change of this screen passes through one 'when': one place to read, log or test.
 * (MVIKotlin calls them Intent and Message, Redux calls the second one Action.)
 */
internal sealed interface Msg {
    data class SensorUpdated(val data: SensorData) : Msg
    data object CalibrationStarted : Msg
    data object CalibrationFinished : Msg
}

/*
 * The one and only reducer: pure, total (exhaustive when), no coroutines, no repository.
 *  'internal' instead of 'private', so a plain JVM unit test can call it, e.g.
 *      assertTrue(reduce(TemperatureViewState(5), Msg.CalibrationStarted).isCalibrating)
 *
 *  The old version ended isCalibrating with the next sensor value. That was a race: a StateFlow
 *  compares values with equals() and drops equal ones, and SensorData is a data class. If the
 *  jitter produced the same value again (about every 5th tick, more often at the range limits),
 *  nothing was emitted and the flag stayed true for seconds. Now the end comes from the
 *  operation itself (CalibrationFinished).
 */
internal fun reduce(state: TemperatureViewState, msg: Msg): TemperatureViewState = when (msg) {
    is Msg.SensorUpdated    -> state.copy(temperature = msg.data.rawValue)
    Msg.CalibrationStarted  -> state.copy(isCalibrating = true)
    Msg.CalibrationFinished -> state.copy(isCalibrating = false)
}

// ----------------------------
// MVI ViewModel ("store")
// ----------------------------

class TemperatureMviViewModel(
    // Constructor injection; the interface instead of the concrete TemperatureRepository.
    private val repository: SensorFlow
) : ViewModel() {

    // seeded from the repository, see TemperatureViewState
    private val _state = MutableStateFlow(TemperatureViewState(temperature = repository.data.value.rawValue))

    // Reducers are pure functions: old State + input -> new State. 'update' applies them
    // atomically (no lost updates if two coroutines change the state at the same time).
    // The funnel: the only place where _state is written.
    private fun accept(msg: Msg) = _state.update { reduce(it, msg) }

    /*
     * Before: 'init { viewModelScope.launch { repository.data.collect { … } } }'.
     *  That collected for the whole life of the ViewModel - in the background, too - and kept the
     *  (now cold) sensor running. MVVM gets lifecycle-awareness for free with
     *  map{}.stateIn(WhileSubscribed); an imperative store needs this small construction:
     *   – channelFlow runs only while somebody collects 'state' (stateIn + WhileSubscribed),
     *   – meanwhile a child coroutine feeds sensor values into the funnel, and every new state
     *     is forwarded downstream,
     *   – when the UI stops collecting (+5s grace for rotation), both stop, and the repository
     *     stops the sensor.
     *  _state itself survives, so isCalibrating etc. are not lost while in the background.
     */
    val state: StateFlow<TemperatureViewState> = channelFlow {
        launch { repository.data.collect { accept(Msg.SensorUpdated(it)) } }
        _state.collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _state.value)

    /*
     * dispatch is the 'executor': it turns intents into side effects and messages.
     *  Side effects (repository calls) live here, never in the reducer. Before, calibrate() was
     *  called right next to the state change, and the order of the two was an accident.
     */
    fun dispatch(intent: TemperatureIntent) {
        when (intent) {
            TemperatureIntent.CalibrateClicked -> calibrate()
        }
    }

    private fun calibrate() {
        // The executor may read the state; here it ignores double taps. No race:
        //  viewModelScope runs on Dispatchers.Main.immediate, so the launch below runs
        //  synchronously up to its first suspension point - CalibrationStarted is reduced before
        //  a second tap can be handled.
        if (_state.value.isCalibrating) return

        viewModelScope.launch {
            accept(Msg.CalibrationStarted)
            try {
                repository.calibrate()
            } finally {
                accept(Msg.CalibrationFinished)     // also on failure or cancellation
            }
        }
    }

    // same factory pattern as in MVVM; this[APPLICATION_KEY] returns Application?
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RawSensorMVIApplication
                TemperatureMviViewModel(app.temperatureRepository)
            }
        }
    }
}
