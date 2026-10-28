// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/*
 * viewModelScope runs on Dispatchers.Main - the Android main thread, which does not exist in a
 * local (JVM) test. This JUnit rule replaces Main by a test dispatcher before every test and
 * restores it afterwards. A StandardTestDispatcher does not run coroutines by itself: they wait
 * until the test says so (advanceUntilIdle), which makes the order of events deterministic.
 * runTest uses the same scheduler, so time in the test (delay!) is virtual - a delay(500) in the
 * code under test takes no real time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
