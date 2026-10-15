// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composable_calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * The logic has no Android or Compose dependency, so it runs as a plain JVM test.
 * 'press' types a sequence of keys like a user would.
 */
class CalculatorLogicTest {

    private fun press(keys: String): CalculatorState =
        keys.filter { it != ' ' }.fold(CalculatorState()) { state, key ->
            state.reduce(
                when (key) {
                    in '0'..'9' -> CalculatorAction.Digit(key - '0')
                    '.' -> CalculatorAction.Decimal
                    '+' -> CalculatorAction.Operation(Operator.Add)
                    '-' -> CalculatorAction.Operation(Operator.Subtract)
                    '*' -> CalculatorAction.Operation(Operator.Multiply)
                    '/' -> CalculatorAction.Operation(Operator.Divide)
                    '=' -> CalculatorAction.Equals
                    's' -> CalculatorAction.Square
                    '<' -> CalculatorAction.Delete
                    'C' -> CalculatorAction.Clear
                    else -> error("unknown key '$key'")
                }
            )
        }

    @Test fun typing() = assertEquals("123.45", press("0123.4.5").display)
    @Test fun addition() = assertEquals("5", press("2 + 3 =").display)
    @Test fun leftToRight() = assertEquals("20", press("2 + 3 * 4 =").display) // pocket calculator style
    @Test fun lastOperatorWins() = assertEquals("6", press("2 + * 3 =").display)
    @Test fun negativeResult() = assertEquals("-4", press("3 - 7 =").display)
    @Test fun decimals() = assertEquals("0.3", press(".1 + .2 =").display) // and not 0.30000000000000004
    @Test fun division() = assertEquals("0.333333333333", press("1 / 3 =").display)
    @Test fun square() = assertEquals("144", press("12 s").display)
    @Test fun squareInExpression() = assertEquals("11", press("2 + 3 s =").display)
    @Test fun delete() = assertEquals("12", press("123 <").display)
    @Test fun deleteKeepsResults() = assertEquals("5", press("2 + 3 = <").display)
    @Test fun newNumberAfterResult() = assertEquals("7", press("2 + 3 = 7").display)
    @Test fun continueWithResult() = assertEquals("10", press("2 + 3 = * 2 =").display)
    @Test fun expressionLine() = assertEquals("12 +", press("12 +").expression)
    @Test fun clear() = assertEquals(CalculatorState(), press("12 + 3 C"))

    @Test fun divisionByZero() {
        val state = press("5 / 0 =")
        assertTrue(state.isError)
        assertEquals("Error", state.display)
        assertEquals("4", press("5 / 0 = 4").display) // a new number recovers
    }
}
