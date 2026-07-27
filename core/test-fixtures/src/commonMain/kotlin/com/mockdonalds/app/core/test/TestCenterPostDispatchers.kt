package com.mockdonalds.app.core.test

import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher

/**
 * Dispatchers for tests.
 *
 * The default is [StandardTestDispatcher], which queues work against a virtual-time scheduler.
 * That is fine — and is what almost every test here wants — for code that only *collects* flows:
 * the work is driven by the collector, so nothing needs to advance the clock.
 *
 * It is NOT fine for code under test that calls `withContext(dispatchers.…)`. A
 * `StandardTestDispatcher` only runs queued work when its scheduler is advanced, and this project
 * bans both `runTest` and `UnconfinedTestDispatcher` (see
 * `.agents/standards/forbidden-patterns.md`) — the only two mechanisms that would advance it. The
 * coroutine therefore suspends forever and the test *hangs* rather than fails.
 *
 * Use [background] for those tests. `OrderRepositoryImpl.refreshIfStale` is currently the only
 * production code in the repo that calls `withContext` on these dispatchers.
 */
class TestCenterPostDispatchers(
    private val dispatcher: CoroutineDispatcher = StandardTestDispatcher(),
) : CenterPostDispatchers {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher

    companion object {
        /**
         * Dispatchers that actually execute, on a real background thread.
         *
         * For code under test that calls `withContext(dispatchers.io)`, where the default
         * [StandardTestDispatcher] would deadlock because nothing advances its scheduler.
         *
         * [Dispatchers.Default] rather than an unconfined dispatcher on purpose: it preserves the
         * production behaviour these tests assert against, where `withContext` genuinely suspends
         * and hops threads — so a flow's cached value still arrives before a refresh completes. An
         * unconfined dispatcher collapses that into synchronous execution and can invert the
         * emission order. It is also safe under `SpecExecutionMode.LimitedConcurrency`, unlike
         * `UnconfinedTestDispatcher`, which shares one scheduler across concurrently-running specs.
         */
        fun background(): TestCenterPostDispatchers =
            TestCenterPostDispatchers(Dispatchers.Default)
    }
}
