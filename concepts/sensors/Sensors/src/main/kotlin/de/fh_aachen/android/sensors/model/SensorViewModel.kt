// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.sensors.service_locator.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SensorViewModel : ViewModel() {
    // instead of DI
    private val sensorRepository: SensorRepository = ServiceLocator.sensorRepository

    /*
    The repository delivers cold flows (callbackFlow): a listener is registered only while
    somebody collects. stateIn turns them into StateFlows held by the ViewModel:
     - WhileSubscribed(5000) keeps the upstream (the sensor listener) active only while the UI
       collects, plus 5 s, so a quick rotation does not unregister and re-register the sensors.
     - In the UI, collectAsStateWithLifecycle stops collecting when the app goes to the
       background (onStop) - so the sensors are switched off automatically, without overriding
       onStart/onStop in the Activity.
    A StateFlow also conflates: if values arrive faster than the UI reads them, only the
    newest one is kept.
    */
    private val stopTimeout = SharingStarted.WhileSubscribed(5000)

    val accelerometerData: StateFlow<FloatArray> = sensorRepository.startAccelerometerUpdates()
        .stateIn(viewModelScope, stopTimeout, floatArrayOf(0f, 0f, 0f))

    val gyroscopeData: StateFlow<FloatArray> = sensorRepository.startGyroscopeUpdates()
        .stateIn(viewModelScope, stopTimeout, floatArrayOf(0f, 0f, 0f))

    val batteryData: StateFlow<Float> = sensorRepository.startBatteryUpdates()
        .stateIn(viewModelScope, stopTimeout, 0f)

    fun getSensorList() = sensorRepository.getSensorList()
}
