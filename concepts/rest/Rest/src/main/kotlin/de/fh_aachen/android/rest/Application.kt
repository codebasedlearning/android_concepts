// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest

import android.app.Application
import de.fh_aachen.android.rest.service_locator.ServiceLocator

/*
 * Internet (HTTP)
 *      ↓
 * OkHttp (low-level HTTP client)
 *      ↓
 * Retrofit (API interface + request/response shaping)
 *      ↓
 * Moshi (JSON serializer/deserializer)
 *      ↓
 * Your Kotlin data classes
 */

class RestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize()
    }
}
