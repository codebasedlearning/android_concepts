// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi

import android.app.Application
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor.TemperatureRepository
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
 * MVVM with StateFlow is unidirectional, too (state down,
 * function calls up). MVI is a stricter dialect of UDF. On top of MVVM it adds
 *   - one immutable state object instead of several observable properties,
 *   - one closed input type (sealed intents) instead of arbitrary public functions,
 *   - one pure reducer through which every state change has to pass.
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
 * More precisely: old State + Msg → new State. Intents are what the user wants,
 * messages are what has happened; see Msg in TemperatureMviViewModel.kt.
 *
 * In Jetpack Compose:
 *  – composables collect the StateFlow<State>
 *  – whenever the state changes, Compose automatically recomposes the UI
 *
 * Compose handles diffing and only redraws the parts that rely on changed fields.
 * More precisely: Compose re-runs (recomposes) the restartable composables that
 *  *read* a State that has changed. CityRoute reads the whole state object, so it recomposes on
 *  every change; the composables it calls are skipped if their (stable) parameters are equal.
 *  That is why CityBox gets single fields, not the whole state object. And recomposition is not
 *  redrawing: layout and drawing are separate phases after composition.
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

/*
 * The static ServiceLocator (data class + companion object + 'lateinit instance') is gone.
 *  It hid the dependency: TemperatureMviViewModel reached out to a global, so it could not be
 *  created in a unit test without a running Application, and it depended on the concrete
 *  TemperatureRepository instead of the SensorFlow interface.
 *  Now this class is only the 'composition root' that creates the long-lived objects; the
 *  ViewModel receives the repository through its constructor (TemperatureMviViewModel.Factory).
 *  Identical to RawSensorMVVM, so the two apps differ only in the pattern.
 *
 * use DI if you like (Koin, Hilt) - they generate/replace exactly this factory
 */
class RawSensorMVIApplication : Application() {
    // SupervisorJob ensures that a failing child coroutine does not cancel the others.
    // eager instead of 'by lazy', see the lazy-or-eager rule in RawSensorMVVM's Application.kt
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val temperatureRepository: SensorFlow = TemperatureRepository(appScope)
}
