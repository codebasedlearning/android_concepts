// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi

import android.app.Application
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
