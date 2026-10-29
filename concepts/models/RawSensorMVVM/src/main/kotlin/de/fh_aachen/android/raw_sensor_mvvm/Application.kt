// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm

import android.app.Application
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/*
 * AppContainer: our tiny 'composition root', the one place that creates the long-lived objects
 *  (the app-wide singletons) and wires them together. ViewModels receive what they need via
 *  constructor injection, see TemperatureViewModel.Factory. RawSensorMVI has the very same container.
 *
 *  - A plain class, no Android types: a test can build its own container, or none at all.
 *  - It lives exactly as long as whoever holds it - here the Application, i.e. the process.
 *  - Singletons live here, ViewModels never do. A ViewModel belongs to a screen
 *    (ViewModelStoreOwner): it is scoped, cleared (onCleared, viewModelScope) and gets a
 *    SavedStateHandle. A ViewModel held by the Application gets none of that.
 *  - Shared state between screens goes into a repository (StateFlow) in here, not into a
 *    shared ViewModel.
 *  - The rule for lazy: use it when construction is expensive, has side effects, or needs
 *    something that doesn't exist yet. Otherwise, construct eagerly. TemperatureRepository is
 *    cheap and has no side effects (cold flow), so it is eager.
 *  - SupervisorJob ensures that a failing child coroutine does not cancel the others.
 */
class AppContainer {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val temperatureRepository: SensorFlow = TemperatureRepository(appScope)
}

class RawSensorMVVMApplication : Application() {
    // Created together with the Application object, before any Activity or ViewModel exists.
    //  Once the container needs a Context (database, DataStore, SensorManager), switch to
    //      lateinit var container: AppContainer
    //      override fun onCreate() { super.onCreate(); container = AppContainer(this) }
    //  In a field initializer the Application is not attached to its base context yet, so
    //  'this' as Context would fail there. And pass the Application, never an Activity (leak).
    val container = AppContainer()
}

/* Remarks / DI patterns
  - Without a container, the Application holds the repositories directly
        val temperatureRepository: SensorFlow = TemperatureRepository(appScope)
    Fine for one or two objects; the container starts to pay off when there is more to wire
    up, and it keeps the Application class free of it.

  - Use a global/static ServiceLocator as source for the repositories.
    Removed from RawSensorMVI. A static 'lateinit instance' hides the dependency:
     the ViewModel reaches out to a global, so it cannot be created in a unit test without a
     running Application, and nothing in its signature tells you what it needs.
     Constructor injection makes the dependency explicit; the factory is the price for it.

  - use DI if you like, e.g. Koin, Hilt (they generate/replace exactly this container and the factory)
*/
