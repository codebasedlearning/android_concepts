// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm

import android.app.Application
import de.fh_aachen.android.raw_sensor.SensorFlow
import de.fh_aachen.android.raw_sensor.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/* Remarks
  - The rule is: use lazy when construction is expensive, has side effects,
    or needs something that doesn’t exist yet. Otherwise, construct eagerly.
    The scope fails all three tests.
    The repository used to pass the second test (it started its loop in init).
     Its flow is cold now (see :models-RawSensorLib), so construction is cheap and free of side
     effects -> it fails all three tests, too, and is created eagerly.
  - SupervisorJob ensures that a failing child coroutine does not cancel the others.
  - This class is our tiny 'composition root': the one place that creates the
     long-lived objects. ViewModels receive them via constructor injection, see
     TemperatureViewModel.Factory. RawSensorMVI does exactly the same now.
*/
class RawSensorMVVMApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val temperatureRepository: SensorFlow = TemperatureRepository(appScope)
}

/* Remarks / DI patterns
  - Use an AppContainer if there is more to do, or more data to hold, e.g.
        class AppContainer(appScope: CoroutineScope) {
            val temperatureRepository: SensorFlow = TemperatureRepository(appScope)
        }
    with RawSensorMVVMApplication
        val container = AppContainer(appScope)

  - Use a global/static ServiceLocator as source for the repositories.
    Removed from RawSensorMVI. A static 'lateinit instance' hides the dependency:
     the ViewModel reaches out to a global, so it cannot be created in a unit test without a
     running Application, and nothing in its signature tells you what it needs.
     Constructor injection makes the dependency explicit; the factory is the price for it.

  - use DI if you like, e.g. Koin, Hilt (they generate/replace exactly this factory)
*/
