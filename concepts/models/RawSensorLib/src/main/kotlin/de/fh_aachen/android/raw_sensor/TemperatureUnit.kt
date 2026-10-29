// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor

import kotlin.math.roundToInt

/*
 * A user choice, not sensor data: the sensor always delivers °C, the unit only changes how we
 * show it. That is exactly the kind of state that belongs into a SavedStateHandle:
 *      save what the user did, reload what the app fetched.
 *
 * It lives here because both apps use it, and it is plain Kotlin - no Compose, no Android.
 * enum classes are Serializable, so a SavedStateHandle (in the end a Bundle) stores them as they are.
 */
enum class TemperatureUnit(val symbol: String) {
    Celsius("°C"),
    Fahrenheit("°F");

    fun fromCelsius(celsius: Int): Int = when (this) {
        Celsius -> celsius
        Fahrenheit -> (celsius * 9 / 5.0 + 32).roundToInt()
    }

    fun toggled(): TemperatureUnit = when (this) {
        Celsius -> Fahrenheit
        Fahrenheit -> Celsius
    }
}
