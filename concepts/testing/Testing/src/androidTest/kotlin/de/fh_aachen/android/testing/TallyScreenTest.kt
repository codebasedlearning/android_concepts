// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.testing

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/*
 * 3. Instrumented UI tests (device or emulator). createComposeRule() starts an empty Activity,
 *    setContent puts our composable into it. Then we find nodes (onNodeWithText, ...), act on
 *    them (performClick, ...) and assert (assertIsDisplayed, ...). Compose waits until the UI is
 *    idle between these steps - no sleeps needed.
 *
 * We search the way a user sees the screen: by text. Texts from strings.xml are read from the
 * resources, not copied into the test - otherwise every translation would break the test.
 */
class TallyScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    // the stateless screen: we choose the state, and record the actions it reports
    @Test
    fun showsTheCount() {
        composeRule.setContent {
            TallyScreen(state = TallyUiState(TallyState(count = 3), isLoading = false), onAction = {})
        }
        composeRule.onNodeWithText(context.getString(R.string.tally_count, 3)).assertIsDisplayed()
    }

    @Test
    fun plusReportsAnIncrement() {
        val actions = mutableListOf<TallyAction>()
        composeRule.setContent {
            TallyScreen(state = TallyUiState(isLoading = false), onAction = { actions += it })
        }

        composeRule.onNodeWithText("+").performClick()

        assertEquals(listOf(TallyAction.Increment), actions)
    }

    @Test
    fun buttonsAreDisabledWhileLoading() {
        composeRule.setContent {
            TallyScreen(state = TallyUiState(isLoading = true), onAction = {})
        }
        composeRule.onNodeWithText("+").assertIsNotEnabled()
    }

    // the whole screen with a real ViewModel - but a fake repository (no delays, nothing stored)
    @Test
    fun tappingPlusUpdatesTheCount() {
        val repository = object : TallyRepository {
            override suspend fun load() = 0
            override suspend fun save(count: Int) {}
        }
        composeRule.setContent {
            TallyRoute(viewModel = TallyViewModel(repository))
        }

        composeRule.onNodeWithText("+").performClick()
        composeRule.onNodeWithText("+").performClick()

        composeRule.onNodeWithText(context.getString(R.string.tally_count, 2)).assertIsDisplayed()
    }
}
