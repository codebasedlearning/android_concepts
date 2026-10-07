// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm

import android.app.Application
import de.fh_aachen.android.raw_sensor_mvvm.sensors.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// use DI if you like

data class ServiceLocator(
    val temperatureRepository: TemperatureRepository
)

class RawSensorMVVMApplication : Application() {
    companion object {
        private lateinit var instance: RawSensorMVVMApplication

        val serviceLocator by lazy {
            ServiceLocator(
                instance.temperatureRepository
            )
        }
    }

    // SupervisorJob ensures that a failing child coroutine does not cancel the others.
    private val appScope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    private val temperatureRepository by lazy { TemperatureRepository(appScope) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
