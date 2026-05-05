package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the iOS bridge lifecycle contract: MainScope() in iosMain leaks for the lifetime
 * of the process because it has no parent to cancel it. The single allowed call site is
 * CircuitPresenterKotlinBridge's constructor default, paired with cancel() invoked from the
 * Swift @StateObject holder's deinit. Anywhere else, we want a Konsist failure rather than
 * discover the leak through a profiler.
 *
 * If a future bridge legitimately needs MainScope(), pair it with a cancel() (and add the
 * file to allowedFiles below) — the rule encodes a contract, not a blanket ban.
 */
class IosBridgeLifecycleTest : BehaviorSpec({

    val allowedPathSuffixes = listOf(
        "/composeApp/src/iosMain/kotlin/com/mockdonalds/app/bridge/CircuitPresenterKotlinBridge.kt",
    )

    Given("MainScope() usage in iosMain") {
        Then("only allowed bridge files may import kotlinx.coroutines.MainScope") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..iosMain..") }
                .filter { file -> file.imports.any { it.name == "kotlinx.coroutines.MainScope" } }
                .filter { file -> allowedPathSuffixes.none { suffix -> file.path.endsWith(suffix) } }
                .map { "  ${it.path}" }

            assert(violators.isEmpty()) {
                "MainScope() in iosMain leaks because nothing cancels it. Use a cancellable scope " +
                    "tied to a Swift holder's deinit (see CircuitPresenterKotlinBridge + " +
                    "CircuitPresenterHolder in .agents/standards/ios-interop.md).\nViolators:\n" +
                    violators.joinToString("\n")
            }
        }
    }
})
