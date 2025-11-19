// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.ref.WeakReference

/*
Benefits of callbackFlow:

1. Transforms Callbacks into Reactive Streams:
    Many Android APIs are event-based and use callbacks (e.g., BroadcastReceiver,
    listeners for UI events, etc.). callbackFlow allows you to capture these callbacks
    as a reactive Flow, which fits well with Kotlin’s coroutine-based reactive programming approach.
    Instead of managing callbacks manually, you get a Flow that emits values as they arrive,
    which simplifies code.
2. Asynchronous and Cold by Design:
    callbackFlow is cold. It only starts collecting data when you actively collect the Flow,
    making it ideal for event streams that might otherwise be resource-intensive if started
    immediately. It also runs asynchronously within a coroutine scope, so it integrates
    seamlessly with suspend functions and structured concurrency, avoiding blocking the main thread.
3. Automatic Resource Management:
    With callbackFlow, you can use awaitClose to manage resources automatically. When the
    Flow collection is canceled (e.g., due to a lifecycle change), awaitClose is triggered,
    allowing you to clean up resources like receivers, listeners, or subscriptions.
    This helps prevent memory leaks and resource mismanagement, which are common issues in
    callback-based APIs.
4. Error Handling with Exception Safety:
    callbackFlow handles exceptions internally, providing a safe environment for handling and
    retrying errors if necessary. This is especially helpful in Android, where network or
    event-based data might throw errors, ensuring that your Flow won’t crash unexpectedly.
5. Flexible Emission Control:
    Inside callbackFlow, you can control when and how often values are emitted using trySend.
    This lets you handle cases where data is intermittent or event-based
    (like battery level updates). If trySend fails (e.g., due to a closed channel), it won’t
    throw an exception, making emission control smooth and safe.
*/

class SensorRepository(val context: WeakReference<Context>) {
    private val sensorManager = context.get()
        ?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        // ?: throw IllegalStateException("SensorManager not available")

    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun startAccelerometerUpdates(): Flow<FloatArray> = callbackFlow {
        var prevData = floatArrayOf(0f, 0f, 0f)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (/*it.sensor==accelerometer && */ !prevData.contentEquals(it.values)) {
                        trySend(it.values)
                        prevData = it.values.copyOf()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // register the listener
        accelerometer?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // ensure the listener is unregistered when the flow is closed
        awaitClose {
            sensorManager?.unregisterListener(listener)
        }
    }

    fun startGyroscopeUpdates(): Flow<FloatArray> = callbackFlow {
        var prevData = floatArrayOf(0f, 0f, 0f)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (/*it.sensor==gyroscope && */ !prevData.contentEquals(it.values)) {
                        trySend(it.values)
                        prevData = it.values.copyOf()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        gyroscope?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        awaitClose {
            sensorManager?.unregisterListener(listener)
        }
    }

    /*
    As of API 21 there is a battery manager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    but you have to poll it
        while (true) {
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            emit(level)
            delay(60_000)
    so we use the 'old' approach via BroadcastReceiver instead.
    And taking the opportunity to introduce the concept.
     */
    fun startBatteryUpdates(): Flow<Float> = callbackFlow {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // level range is 0..ExtraScale
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    val batteryPct = level * 100 / scale.toFloat()
                    trySend(batteryPct)
                }
            }
        }

        // register the receiver with the ACTION_BATTERY_CHANGED filter
        context.get()?.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // ensure the receiver is unregistered when the flow is closed
        awaitClose { context.get()?.unregisterReceiver(batteryReceiver) }
    }

    fun getSensorList() = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
}
