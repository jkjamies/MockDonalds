package com.jkjamies.sampleplatter.core.test

import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher

/**
 * [CenterPostDispatchers] for tests: all three route to one [StandardTestDispatcher].
 *
 * ## Advance the scheduler, or dispatched work never runs
 *
 * A `StandardTestDispatcher` queues onto a [TestCoroutineScheduler], and that queue only drains
 * when something advances it. `runTest` normally does that, and it is banned here (see
 * `.agents/standards/forbidden-patterns.md`) — Kotest supplies the coroutine context instead. So
 * a test whose subject dispatches must call [advanceUntilIdle] itself.
 *
 * This matters for any code doing `withContext(dispatchers.io) { … }`, the documented CenterPost
 * pattern for bracketing blocking work. Without an advance, that call never resumes — and the
 * resulting hang cannot be timed out, because cancelling the stuck coroutine requires resuming
 * its continuation on the very scheduler that is not running. `withTimeout` does not fire,
 * Turbine's `awaitItem` timeout does not fire, and the Gradle test task hangs until something
 * kills it from outside. That is exactly what happened to
 * `:features:order:impl:data:testAndroidHostTest`, which ran until CI's task timeout killed it
 * at ten minutes with its first test still reported as SKIPPED.
 *
 * So: if the code under test dispatches, advance before awaiting the result of that work, and
 * again before cancelling collection so nothing is left parked on the queue.
 *
 * ```kotlin
 * repository.getMenuItemsByCategory("burgers").test {
 *     fixture.advanceUntilIdle()
 *     awaitItem() shouldContain cachedItem
 *     fixture.advanceUntilIdle()
 *     cancel()
 * }
 * ```
 *
 * Most suites never need this — they pass this object to a subject that only stores it. It is
 * required wherever production code actually schedules on the injected dispatchers.
 */
class TestCenterPostDispatchers(
    val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
) : CenterPostDispatchers {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher(scheduler)

    override val default: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher

    /** Runs everything currently queued on [scheduler], including work queued by that work. */
    fun advanceUntilIdle() {
        scheduler.advanceUntilIdle()
    }
}
