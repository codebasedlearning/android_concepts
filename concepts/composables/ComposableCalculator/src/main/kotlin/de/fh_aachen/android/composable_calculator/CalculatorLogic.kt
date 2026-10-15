// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composable_calculator

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/*
 * The calculator logic - deliberately plain Kotlin without any Compose import.
 *
 *  - 'CalculatorState' is an immutable snapshot of everything the UI needs to show.
 *  - 'CalculatorAction' is everything the user can do (one key press = one action).
 *  - 'reduce' computes the next state from the current state and an action.
 *
 * The UI only displays a state and reports actions, it never calculates anything itself.
 * This makes the logic easy to unit test (see src/test) and it is a small preview of
 * unidirectional data flow (UDF) and MVI, see 'models'.
 *
 * It works like a simple pocket calculator: an operator is applied as soon as the next one is
 * pressed, there is no 'multiplication before addition'. So 2 + 3 × 4 = 20, not 14.
 * Mathematicians may complain, pocket calculators from the 80s will agree.
 */

enum class Operator(val symbol: String) {
    Add("+"), Subtract("−"), Multiply("×"), Divide("÷");

    // null signals an invalid operation, i.e. division by zero
    fun apply(a: Double, b: Double): Double? = when (this) {
        Add -> a + b
        Subtract -> a - b
        Multiply -> a * b
        Divide -> if (b == 0.0) null else a / b
    }
}

sealed interface CalculatorAction {
    data class Digit(val digit: Int) : CalculatorAction
    data class Operation(val operator: Operator) : CalculatorAction
    data object Decimal : CalculatorAction
    data object Square : CalculatorAction
    data object Equals : CalculatorAction
    data object Delete : CalculatorAction
    data object Clear : CalculatorAction
}

data class CalculatorState(
    val display: String = "0",              // number in the display, as typed or as a result
    val accumulator: Double? = null,        // left operand, waiting for the right one
    val pendingOperator: Operator? = null,  // operator to apply on accumulator and display
    val overwrite: Boolean = true,          // next digit replaces the display (after result etc.)
    val waitingForOperand: Boolean = false, // operator just pressed, no right operand typed yet
    val isError: Boolean = false,
) {
    // small line above the display, e.g. "12 +"
    val expression: String
        get() = if (accumulator != null && pendingOperator != null)
            "${formatNumber(accumulator)} ${pendingOperator.symbol}" else ""

    private val value: Double get() = display.toDoubleOrNull() ?: 0.0

    fun reduce(action: CalculatorAction): CalculatorState {
        // after an error only 'C' or a new number make sense
        if (isError) return when (action) {
            is CalculatorAction.Digit, CalculatorAction.Decimal -> CalculatorState().reduce(action)
            CalculatorAction.Clear -> CalculatorState()
            else -> this
        }

        return when (action) {
            is CalculatorAction.Digit -> when {
                overwrite -> copy(display = "${action.digit}", overwrite = false, waitingForOperand = false)
                display == "0" -> copy(display = "${action.digit}")
                display.length >= MAX_INPUT_LENGTH -> this
                else -> copy(display = display + action.digit)
            }

            CalculatorAction.Decimal -> when {
                overwrite -> copy(display = "0.", overwrite = false, waitingForOperand = false)
                '.' in display -> this
                else -> copy(display = "$display.")
            }

            CalculatorAction.Delete -> when {
                overwrite -> this // results are not edited, only typed numbers
                else -> copy(display = display.dropLast(1).ifEmpty { "0" })
            }

            // squares only the display, a pending operation stays, e.g. "2 + 3 x²" is "2 + 9"
            CalculatorAction.Square -> {
                val result = value * value
                if (result.isInfinite()) error()
                else copy(display = formatNumber(result), overwrite = true, waitingForOperand = false)
            }

            is CalculatorAction.Operation -> when {
                // first operator, e.g. "12 +"
                accumulator == null || pendingOperator == null ->
                    copy(accumulator = value, pendingOperator = action.operator,
                        overwrite = true, waitingForOperand = true)
                // operator pressed twice, e.g. "12 + ×", the last one wins
                waitingForOperand -> copy(pendingOperator = action.operator)
                // chained, e.g. "12 + 3 ×" calculates 15 first
                else -> {
                    val result = pendingOperator.apply(accumulator, value)
                    if (result == null) error()
                    else copy(display = formatNumber(result), accumulator = result,
                        pendingOperator = action.operator, overwrite = true, waitingForOperand = true)
                }
            }

            CalculatorAction.Equals -> when {
                accumulator == null || pendingOperator == null -> this
                // note: "5 + =" uses the display as right operand, so it is 10
                else -> pendingOperator.apply(accumulator, value)?.let { showResult(it) } ?: error()
            }

            CalculatorAction.Clear -> CalculatorState()
        }
    }

    private fun showResult(result: Double): CalculatorState =
        if (result.isNaN() || result.isInfinite()) error()
        else CalculatorState(display = formatNumber(result))

    private fun error() = CalculatorState(display = "Error", isError = true)

    companion object {
        const val MAX_INPUT_LENGTH = 15
    }
}

/*
 * Doubles are binary fractions, so 0.1 + 0.2 = 0.30000000000000004. To hide this, we round to
 * 12 significant digits and drop trailing zeros. Very large or very small numbers are shown
 * in scientific notation like 1.5E+20 (which 'toDoubleOrNull' can parse again).
 */
fun formatNumber(value: Double): String {
    if (value == 0.0) return "0" // also catches -0.0
    val rounded = BigDecimal(value).round(MathContext(12)).stripTrailingZeros()
    return if (abs(value) >= 1e12 || abs(value) < 1e-6) rounded.toString() else rounded.toPlainString()
}
