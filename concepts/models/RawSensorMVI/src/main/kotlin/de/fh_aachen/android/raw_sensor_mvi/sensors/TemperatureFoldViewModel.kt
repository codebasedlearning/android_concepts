// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor_mvi.RawSensorMVIApplication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform

/*
 * The purest form of UDF: the state is a fold (scan) over all messages that ever happened,
 *      state = fold(initialState, messages, reduce)
 * Same Intent, Msg and reduce as TemperatureMviViewModel - only the store differs: no
 * MutableStateFlow, no update(), the whole screen state is one declarative expression.
 *
 * To try it, change the parameter of CityRoute in MainActivity.kt to
 *      viewModel: TemperatureFoldViewModel = viewModel(factory = TemperatureFoldViewModel.Factory)
 * 'state' and 'dispatch' have the same signatures.
 *
 * Honest caveats - why the imperative store is the default:
 *  1. Intents need a Channel. A SharedFlow drops emissions while nobody collects; a Channel
 *     buffers them until the fold runs again.
 *  2. With WhileSubscribed, the fold *restarts* from initialState after the UI was gone for
 *     more than 5s. Fine for values derived from the sensor, wrong for real user state (typed
 *     text, a selection). SharingStarted.Eagerly keeps it - at the price of collecting in the
 *     background again.
 *  3. The executor (transform below) cannot see the current state, so it cannot ignore a double
 *     tap: a second CalibrateClicked is queued and calibrates twice (the button is disabled in
 *     the UI, though). MVIKotlin, for example, solves this by handing its executor a getState().
 */
class TemperatureFoldViewModel(
    private val repository: SensorFlow
) : ViewModel() {

    private val intents = Channel<TemperatureIntent>(Channel.UNLIMITED)

    fun dispatch(intent: TemperatureIntent) {
        intents.trySend(intent)
    }

    // executor: intents -> side effects -> messages; transform handles one intent after the other
    private val intentMsgs: Flow<Msg> = intents.receiveAsFlow().transform { intent ->
        when (intent) {
            TemperatureIntent.CalibrateClicked -> {
                emit(Msg.CalibrationStarted)
                repository.calibrate()
                emit(Msg.CalibrationFinished)
            }
        }
    }

    private val sensorMsgs: Flow<Msg> = repository.data.map { Msg.SensorUpdated(it) }

    private val initialState = TemperatureViewState(temperature = repository.data.value.rawValue)

    val state: StateFlow<TemperatureViewState> =
        merge(sensorMsgs, intentMsgs)
            .scan(initialState) { acc, msg -> reduce(acc, msg) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RawSensorMVIApplication
                TemperatureFoldViewModel(app.temperatureRepository)
            }
        }
    }
}
