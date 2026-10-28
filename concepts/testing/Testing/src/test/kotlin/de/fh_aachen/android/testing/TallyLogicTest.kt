// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.testing

import org.junit.Assert.assertEquals
import org.junit.Test

/*
 * 1. Local unit tests of pure logic: no Android, no coroutines, no setup. A JUnit test is a
 *    public function annotated with @Test; it passes if it does not throw. assertEquals(expected,
 *    actual) throws with a readable message if the values differ.
 *
 * Name tests after the behaviour, not after the function ('decrementNeverGoesBelowZero', not
 * 'testReduce2'): a failing test name should tell you what broke.
 */
class TallyLogicTest {

    // types a sequence of actions like a user would, cf. CalculatorLogicTest
    private fun press(vararg actions: TallyAction, start: TallyState = TallyState()): TallyState =
        actions.fold(start) { state, action -> reduce(state, action) }

    @Test
    fun incrementAddsTheStep() {
        assertEquals(5, press(TallyAction.ChangeStep(5), TallyAction.Increment).count)
    }

    @Test
    fun decrementNeverGoesBelowZero() {
        assertEquals(0, press(TallyAction.Increment, TallyAction.Decrement, TallyAction.Decrement).count)
    }

    @Test
    fun resetKeepsTheStep() {
        val state = press(TallyAction.ChangeStep(10), TallyAction.Increment, TallyAction.Reset)
        assertEquals(TallyState(count = 0, step = 10), state)      // data classes compare by value
    }

    @Test
    fun stepIsLimitedToOneToTen() {
        assertEquals(1, press(TallyAction.ChangeStep(0)).step)
        assertEquals(10, press(TallyAction.ChangeStep(99)).step)
    }
}
