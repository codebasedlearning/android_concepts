// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi

import android.app.Application
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/*
 * Idea behind MVI
 *
 * MVI (Model–View–Intent) is basically unidirectional data-flow with
 * one single source of truth (State).
 *      Intent → Reduce → State → UI …again.
 *
 * In MVI, your UI doesn’t observe many pieces of data. Instead you keep
 * one immutable State object. Here this is TemperatureViewState.
 * This state describes the entire UI at this moment. Composables only ever
 * read this.
 *
 * The UI never calls arbitrary functions on the ViewModel. Instead it sends
 * Intents (no Android-Intents, App-specific). Here this is TemperatureIntent.
 *
 * The reducer is the function that takes
 *      old State + Event/Intent → new State
 * That is, it returns a brand-new state object, rather than mutating old values.
 *
 * In Jetpack Compose:
 *  – composables collect the StateFlow<State>
 *  – whenever the state changes, Compose automatically recomposes the UI
 *
 * Compose handles diffing and only redraws the parts that rely on changed fields.
 * Then:
 *  – Your app is just a state machine
 *  – The UI is just a function of that state
 *
 * Cycle:
 *  – User taps → Intent
 *  – Intent → ViewModel
 *  – ViewModel reduces → new State
 *  – StateFlow emits → Compose re-renders UI
 *  – UI shows new State → user taps again
 */

// use DI if you like

data class ServiceLocator(
    val temperatureRepository: TemperatureRepository
)

class RawSensorMVIApplication : Application() {
    companion object {
        private lateinit var instance: RawSensorMVIApplication

        val serviceLocator by lazy {
            ServiceLocator(
                instance.temperatureRepository
            )
        }
    }

    // SupervisorJob grants that in case of an error other child coroutines are not cancelled.
    private val appScope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    private val temperatureRepository by lazy { TemperatureRepository(appScope) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
