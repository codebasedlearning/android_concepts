// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi.sensors

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.raw_sensor.SensorData
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor.TemperatureUnit
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
 *  – The persisted part of the state (the unit) lives in a SavedStateHandle and enters the
 *    store as messages, like the sensor values.
 */

// ----------------------------
// MVI: State + Intent + Msg
// ----------------------------

data class TemperatureViewState(
    // No default '= 0' any more. 0 °C is a valid temperature, so it was a bad
    //  'not loaded yet' marker; the ViewModel seeds the state from the repository instead.
    val temperature: Int,                               // always °C, as the sensor delivers it
    val unit: TemperatureUnit = TemperatureUnit.Celsius,
    val isCalibrating: Boolean = false,
    // more fields like
    // val error: String? = null
) {
    // Derived, not stored: a stored 'displayTemperature' could get out of sync with
    //  temperature and unit. A plain getter in a data class is not part of equals/copy.
    val displayTemperature: Int get() = unit.fromCelsius(temperature)
}

/**
 * Intents = everything the View "wants to do".
 * User actions only here; sensor updates are handled internally.
 */
sealed interface TemperatureIntent {
    data object CalibrateClicked : TemperatureIntent      // 'data object': readable toString()
    data object ToggleUnitClicked : TemperatureIntent

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
    data class UnitChanged(val unit: TemperatureUnit) : Msg
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
    is Msg.UnitChanged      -> state.copy(unit = msg.unit)
}

// ----------------------------
// MVI ViewModel ("store")
// ----------------------------

class TemperatureMviViewModel(
    // Constructor injection; the interface instead of the concrete TemperatureRepository.
    private val repository: SensorFlow,
    private val savedStateHandle: SavedStateHandle,     // a test simply passes SavedStateHandle()
) : ViewModel() {

    /*
     * SavedStateHandle in MVI: only a *slice* of the state is saved.
     *  Rule: save what the user did, reload what the app fetched.
     *   - unit:          user choice -> handle, survives process death.
     *   - temperature:   sensor data -> reloaded.
     *   - isCalibrating: never! The calibration coroutine dies with the process; a restored
     *                    'true' would show a spinner for an operation nobody runs any more.
     *  So we do not save the whole TemperatureViewState (that would also need it to be
     *  Parcelable, and it would grow with every field).
     *
     *  Who owns the unit? The handle - it is the single source of truth, the store state only
     *  mirrors it. The executor writes the handle (toggleUnit), the handle's StateFlow feeds
     *  UnitChanged into the funnel, like the sensor feeds SensorUpdated. So the reducer stays
     *  pure and still sees every state change, and there is no second copy of the unit that
     *  could get out of sync.
     *  (Alternative: write the handle inside accept() after every reduce. Works, but then a
     *  side effect sits in the funnel, and it runs once per sensor tick.)
     */
    private val savedUnit: StateFlow<TemperatureUnit> = savedStateHandle.getStateFlow(KEY_UNIT, TemperatureUnit.Celsius)

    // seeded from the repository and the handle, see TemperatureViewState
    private val _state = MutableStateFlow(
        TemperatureViewState(temperature = repository.data.value.rawValue, unit = savedUnit.value)
    )

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
     *   – meanwhile child coroutines feed sensor values and unit changes into the funnel, and
     *     every new state is forwarded downstream,
     *   – when the UI stops collecting (+5s grace for rotation), both stop, and the repository
     *     stops the sensor.
     *  _state itself survives, so isCalibrating etc. are not lost while in the background.
     */
    val state: StateFlow<TemperatureViewState> = channelFlow {
        launch { repository.data.collect { accept(Msg.SensorUpdated(it)) } }
        launch { savedUnit.collect { accept(Msg.UnitChanged(it)) } }
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
            TemperatureIntent.ToggleUnitClicked -> toggleUnit()
        }
    }

    // Side effect: write the handle. The new unit comes back as Msg.UnitChanged (see savedUnit).
    private fun toggleUnit() {
        savedStateHandle[KEY_UNIT] = savedUnit.value.toggled()
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
    //  createSavedStateHandle() restores what was saved for this ViewModel (after process death)
    //  and registers the handle for the next snapshot, see TemperatureViewModel in RawSensorMVVM.
    companion object {
        internal const val KEY_UNIT = "unit"    // internal: the fold store uses the same key

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RawSensorMVIApplication
                TemperatureMviViewModel(
                    repository = app.container.temperatureRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
