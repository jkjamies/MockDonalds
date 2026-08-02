package com.jkjamies.sampleplatter.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates test conventions:
 * - Test classes end with Test or Tests suffix
 * - All specs use BehaviorSpec (consistent style)
 * - No runBlocking or runTest (Kotest handles coroutines)
 * - No UnconfinedTestDispatcher (unsafe with concurrent specs)
 * - No hand-rolled StandardTestDispatcher (hides the scheduler, so it can never be advanced)
 */
class TestFileNamingTest : BehaviorSpec({

    val kotestSpecStyles = listOf(
        "BehaviorSpec", "FunSpec", "StringSpec", "DescribeSpec",
        "WordSpec", "FreeSpec", "ShouldSpec", "FeatureSpec",
        "ExpectSpec", "AnnotationSpec",
    )

    Given("test file naming") {
        Then("all test classes should end with Test or Tests suffix") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") || it.resideInPath("..test/..") }
                .filter { it.hasParent { p -> p.name in kotestSpecStyles } }
                .assertTrue(additionalMessage = "Test classes must end with 'Test' or 'Tests' suffix") {
                    it.name.endsWith("Test") || it.name.endsWith("Tests")
                }
        }
    }

    Given("consistent spec style") {
        Then("all test specs should extend BehaviorSpec") {
            val nonBehaviorSpecs = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .filter { klass ->
                    klass.hasParent { p -> p.name in kotestSpecStyles && p.name != "BehaviorSpec" }
                }

            assert(nonBehaviorSpecs.isEmpty()) {
                val names = nonBehaviorSpecs.joinToString("\n") { "  ${it.name} (${it.path})" }
                "All test specs must extend BehaviorSpec for consistent Given/When/Then style:\n$names"
            }
        }
    }

    Given("no blocking coroutine patterns in tests") {
        // `core:test-fixtures` is test infrastructure that happens to live in commonMain (it is
        // consumed as a normal dependency by every module's commonTest). Scoping these rules to
        // `..commonTest..` alone left the shared fixtures — the highest-blast-radius test code
        // in the repo — unchecked, which is exactly where the StandardTestDispatcher hang came
        // from.
        val testFiles = Konsist.scopeFromProject()
            .files
            .filter { it.resideInPath("..commonTest..") || it.resideInPath("..core/test-fixtures..") }

        Then("no runBlocking in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.runBlocking" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "runBlocking is not allowed in tests — Kotest handles coroutines natively:\n$names"
            }
        }

        Then("no runTest in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.test.runTest" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "runTest is not needed — Kotest provides coroutine support natively, use TestCenterPostDispatchers:\n$names"
            }
        }

        Then("no UnconfinedTestDispatcher in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.test.UnconfinedTestDispatcher" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UnconfinedTestDispatcher is not allowed — it is not safe under concurrent spec execution (SpecExecutionMode.LimitedConcurrency). Use TestCenterPostDispatchers:\n$names"
            }
        }

        Then("no raw StandardTestDispatcher outside TestCenterPostDispatchers") {
            // StandardTestDispatcher is the project's chosen test dispatcher, but it queues onto
            // a TestCoroutineScheduler that only drains when something advances it — and
            // `runTest`, the usual thing that does, is banned above. Constructing one ad hoc in a
            // test hides that scheduler, leaving no way to advance it: code under test doing
            // `withContext(dispatchers.io) { … }` then suspends forever, and the hang cannot be
            // timed out, because cancelling it needs that same unadvanced scheduler to resume the
            // continuation. `withTimeout` and Turbine's own timeout both fail to fire and the
            // Gradle test task hangs until something kills it from outside — which is what hung
            // `:features:order:impl:data:testAndroidHostTest` in CI until the task timeout.
            // Going through TestCenterPostDispatchers keeps the scheduler reachable via
            // `advanceUntilIdle()`.
            val violators = testFiles
                // `nameWithExtension`, not `name`: Konsist's file `name` has no extension, so
                // comparing it against "TestCenterPostDispatchers.kt" never matched and the
                // fixture reported itself. Pinned to the canonical path as well, so a module
                // cannot opt out of this rule by naming a local file the same thing.
                .filter {
                    !(
                        it.nameWithExtension == "TestCenterPostDispatchers.kt" &&
                            it.resideInPath("..core/test-fixtures..")
                        )
                }
                // Safe to match on text below: `testFiles` is commonTest + core/test-fixtures,
                // so this rule's own file (testing/architecture-check/src/test) is out of scope
                // and its prose mentions of the symbol cannot flag it. No exclusion needed — one
                // added here would match nothing, which is the failure this suite exists to catch.
                .filter { file ->
                    // Matching the exact import alone left two ways through. The wildcard ban in
                    // CodeHygieneTest is scoped to `isProductionSourcePath`, so a *test* file may
                    // legally write `import kotlinx.coroutines.test.*` and construct one; and a
                    // fully-qualified call needs no import at all.
                    val exactImport = file.imports.any {
                        it.name == "kotlinx.coroutines.test.StandardTestDispatcher"
                    }
                    val wildcardImport = file.imports.any {
                        it.isWildcard && it.name.startsWith("kotlinx.coroutines.test")
                    }
                    // Both patterns tolerate whitespace before the paren: Kotlin accepts
                    // `StandardTestDispatcher ()`, so a literal "...Dispatcher(" substring left a
                    // third way through — a spaced fully-qualified call, which needs no import and
                    // so never reached the wildcard branch either.
                    val constructsIt =
                        Regex("""\bStandardTestDispatcher\s*\(""").containsMatchIn(file.text)
                    val fullyQualified =
                        Regex("""\bkotlinx\.coroutines\.test\.StandardTestDispatcher\s*\(""")
                            .containsMatchIn(file.text)

                    exactImport || fullyQualified || (wildcardImport && constructsIt)
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Construct StandardTestDispatcher through TestCenterPostDispatchers, not " +
                    "directly — a hand-rolled one hides its scheduler, and unadvanced queued " +
                    "work hangs the test unkillably:\n$names"
            }
        }
    }
})
