// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.sensors.SensorsApplication
import de.fh_aachen.android.sensors.service_locator.ServiceLocator
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SensorViewModel() : ViewModel() {
    // instead of DI
    private val sensorRepository: SensorRepository = ServiceLocator.sensorRepository

    // usual combination of MutableState and State (read-only)
    private val _accelerometerData = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val accelerometerData: StateFlow<FloatArray> = _accelerometerData

    private val _gyroscopeData = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val gyroscopeData: StateFlow<FloatArray> = _gyroscopeData

    private val _batteryData = MutableStateFlow(0f)
    val batteryData: StateFlow<Float> = _batteryData

    /*
    Here launch uses 'Dispatchers.Main', an android specific dispatcher able to update UI elements
    The collectLatest operator in Kotlin Flow is a special variant of collect that cancels the
    previous collection if a new value is emitted before the previous one finishes processing
    (that is the lambda).
    This is particularly useful when dealing with fast or frequent data emissions, as it ensures
    that only the most recent value is processed, while intermediate values are discarded.
    */
    fun startListening() {
        viewModelScope.launch {
            sensorRepository.startAccelerometerUpdates()
                .collectLatest { data -> _accelerometerData.value = data.copyOf() }
        }
        viewModelScope.launch {
            sensorRepository.startGyroscopeUpdates()
                .collectLatest { data -> _gyroscopeData.value = data.copyOf() }
        }
        viewModelScope.launch {
            sensorRepository.startBatteryUpdates()
                .collectLatest { data -> _batteryData.value = data }
        }
    }

    fun stopListening() {
        viewModelScope.coroutineContext.cancelChildren()
    }

    fun getSensorList() = sensorRepository.getSensorList()
}
