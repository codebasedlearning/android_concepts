// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors

import android.app.Application
import de.fh_aachen.android.sensors.service_locator.ServiceLocator

class SensorsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(applicationContext)
    }
}
