// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor

import kotlinx.coroutines.flow.StateFlow

// general sensor structures

data class SensorData(val rawValue: Int)        // could contain more values

interface SensorFlow {
    val data: StateFlow<SensorData>

    // 'suspend' instead of a plain fun. Calibrating a real device takes time (and may
    // fail). A suspend function tells the caller when it is *done*, so the UI state
    // 'isCalibrating' can follow the real end of the operation instead of guessing it from the
    // next sensor value (that was a race in the MVI version, see Msg there).
    suspend fun calibrate()                     // dummy functionality
}

// random values
fun ClosedRange<Int>.center() = start + (endInclusive - start) / 2
fun Int.nextJitter(withinRange: ClosedRange<Int>) = (this + (-2..2).random()).coerceIn(withinRange)
