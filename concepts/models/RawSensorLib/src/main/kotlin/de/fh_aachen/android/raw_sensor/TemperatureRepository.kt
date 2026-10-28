// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = "SENSOR"

/*
 * Cold source instead of a hot 'while(true)' started in init.
 *
 * Before: the constructor launched an endless loop in appScope. It ran from the first access
 * until the process died, no matter whether anybody looked at the values.
 * collectAsStateWithLifecycle only stopped the *UI* from listening - the sensor kept going.
 *
 * Now:
 *  - 'flow { ... }' is cold: the body does nothing until somebody collects it.
 *  - 'stateIn(..., WhileSubscribed())' shares one running upstream among all collectors, stops
 *    it when the last collector is gone, and restarts it with the next one.
 *  - So the subscription chain UI -> ViewModel -> Repository decides whether the sensor runs:
 *    demand flows up, data flows down. UDF, applied to resources.
 *  - No side effect in the constructor any more, so the repository can be created eagerly.
 *
 * Why WhileSubscribed() without a timeout here? The ViewModels already use
 * WhileSubscribed(5_000) to survive configuration changes (rotation): during a rotation the
 * ViewModel keeps collecting, so the repository never sees a gap. One grace period is enough;
 * two would just add up (5s + 5s).
 *
 * For a real sensor, the flow would be a callbackFlow - same shape, same sharing operator:
 *      callbackFlow {
 *          val listener = object : SensorEventListener {
 *              override fun onSensorChanged(e: SensorEvent) { trySend(SensorData(e.values[0].roundToInt())) }
 *              override fun onAccuracyChanged(s: Sensor?, accuracy: Int) = Unit
 *          }
 *          sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
 *          awaitClose { sensorManager.unregisterListener(listener) }   // runs when nobody listens
 *      }
 */
class TemperatureRepository(appScope: CoroutineScope) : SensorFlow {
    private val sensorTemperatureRange: ClosedRange<Int> = -5..15

    // Kept outside the flow, so a restart continues where it stopped instead of jumping
    // back to the center. Only the single shared upstream writes it, so no synchronisation needed.
    private var lastValue = sensorTemperatureRange.center()

    override val data: StateFlow<SensorData> = flow {
        while (true) {
            emit(SensorData(rawValue = lastValue))
            Log.i(TAG, "new temperature value: $lastValue")
            delay(1.seconds)
            lastValue = lastValue.nextJitter(withinRange = sensorTemperatureRange)
        }
    }
        // Watch these two in Logcat while you send the app to the background and back.
        .onStart { Log.i(TAG, "sensor started (first subscriber)") }
        .onCompletion { Log.i(TAG, "sensor stopped (no subscribers left)") }
        .stateIn(appScope, SharingStarted.WhileSubscribed(), SensorData(rawValue = lastValue))

    override suspend fun calibrate() {
        Log.i(TAG, "calibrate device")
        // Simulates a device round trip; makes 'isCalibrating' visible in the UI.
        delay(1500.milliseconds)
        // here you could shift/normalize values etc.
        Log.i(TAG, "calibration done")
    }
}
