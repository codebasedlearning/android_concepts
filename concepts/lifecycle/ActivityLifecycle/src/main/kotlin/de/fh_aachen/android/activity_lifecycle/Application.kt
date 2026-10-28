// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.activity_lifecycle

import android.app.Application
import android.util.Log
import de.fh_aachen.android.activity_lifecycle.models.InputDataModel

// Do not forget to register this class in the AndroidManifest.xml ('android:name').

/*
 * There is exactly one Application object per process; it is created before any Activity and
 * lives as long as the process. So its properties survive rotation (the Activity is recreated,
 * the Application is not) - but not process death.
 *
 * Before, a static 'lateinit var instance' in a companion object gave access to it.
 *  Not needed: every Context knows its Application (context.applicationContext), and from a
 *  composable we get a Context via LocalContext - see InputVersion4. No global, no lateinit.
 */
class ActivityLifecycleApplication : Application() {

    val inputModel = InputDataModel()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application.onCreate - a new process")
    }

    // there is no reliable 'onDestroy' for the Application: the process is simply killed
    // (onTerminate is only called in emulated environments)
}
