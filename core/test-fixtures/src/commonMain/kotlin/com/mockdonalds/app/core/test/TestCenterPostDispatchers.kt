package com.mockdonalds.app.core.test

import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * [CenterPostDispatchers] for tests: all three dispatchers run eagerly on the calling thread.
 *
 * ## Why not `StandardTestDispatcher`
 *
 * This fixture used to default to `StandardTestDispatcher()`. That constructor creates its own
 * `TestCoroutineScheduler`, and a scheduler only runs the work queued on it when something
 * advances it — `runTest`, `advanceUntilIdle()` or `runCurrent()`. This project bans `runTest`
 * (see `.agents/standards/forbidden-patterns.md`) and nothing else advanced it, so anything
 * actually dispatched onto these dispatchers was queued and never executed.
 *
 * That made the fixture a trap rather than a test double. Production code doing
 * `withContext(dispatchers.io) { … }` — the documented CenterPost pattern for bracketing
 * blocking work — suspends forever. Worse, the resulting hang is not recoverable by a timeout:
 * cancelling the stuck coroutine requires resuming its continuation, and that resumption is
 * itself dispatched onto the same dead scheduler. `withTimeout` around the collection does not
 * fire, Turbine's `awaitItem` timeout does not fire, and the Gradle test task hangs until
 * something kills it from the outside.
 *
 * It went unnoticed because `OrderRepositoryImpl` is the only repository in the project that
 * dispatches through the injected `CenterPostDispatchers`; every other suite merely passes this
 * object along without ever scheduling on it, so those tests were unaffected either way.
 *
 * ## Why `Dispatchers.Unconfined`
 *
 * It executes on the calling thread with no queue, no scheduler and no shared state, so ordering
 * is deterministic and single-threaded — which is what "deterministic execution" was after in the
 * first place. It is *not* `UnconfinedTestDispatcher`, which remains banned: that one is bound to
 * a `TestCoroutineScheduler` and carries virtual-time state that is unsafe under Kotest's
 * concurrent spec execution. `Dispatchers.Unconfined` has neither.
 *
 * A test that genuinely needs virtual time can still pass its own dispatcher — but it then owns
 * advancing the scheduler.
 */
class TestCenterPostDispatchers(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
) : CenterPostDispatchers {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
