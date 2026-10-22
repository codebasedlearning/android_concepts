// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm

import android.app.Application
import de.fh_aachen.android.raw_sensor_mvvm.sensors.SensorFlow
import de.fh_aachen.android.raw_sensor_mvvm.sensors.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/* Remarks
  - The rule is: use lazy when construction is expensive, has side effects,
    or needs something that doesn’t exist yet. Otherwise, construct eagerly.
    The scope fails all three tests. The repository passes the second one.
  - SupervisorJob ensures that a failing child coroutine does not cancel the others.
*/
class RawSensorMVVMApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val temperatureRepository: SensorFlow by lazy { TemperatureRepository(appScope) }
}

/* Remarks / DI patterns
  - Use an AppContainer if there is more to do, or more data to hold, e.g.
        class AppContainer(appScope: CoroutineScope) {
            val temperatureRepository: SensorFlow by lazy { TemperatureRepository(appScope) }
        }
    with RawSensorMVVMApplication
        val container by lazy { AppContainer(appScope) }

  - Use a global/static ServiceLocator as source for the repositories, e.g.
        data class ServiceLocator(
            val temperatureRepository: TemperatureRepository
        )
    with a companion object in RawSensorMVVMApplication holding the repos
        private lateinit var instance: RawSensorMVVMApplication
        val serviceLocator by lazy { ServiceLocator(instance.temperatureRepository) }
    init in onCreate
        instance = this
    and accessed in TemperatureViewModel (instead of the factory)
        private val repository = RawSensorMVVMApplication.serviceLocator.temperatureRepository
    then you do not need to use the factory in the composables
        val city: TemperatureViewModel = viewModel()

  - use DI if you like, e.g. Koin, Hilt
*/
