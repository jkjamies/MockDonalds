package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates that DI graph infrastructure is placed correctly:
 * - @DependencyGraph only in consumer modules (composeApp, navint-tests)
 * - CircuitProviders only in core:circuit
 * - AppGraph interface only in core:metro
 */
class DependencyGraphScopeTest : BehaviorSpec({

    Given("DependencyGraph placement") {
        Then("@DependencyGraph should only exist in composeApp or test modules") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.hasAnnotation { a -> a.name == "DependencyGraph" } }
                .filter { it.resideInPath("..commonMain..") }
                .filter {
                    !it.resideInPath("..composeApp..") &&
                        !it.resideInPath("..navint-tests..") &&
                        !it.resideInPath("..e2e-tests..")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "@DependencyGraph must only exist in consumer modules (composeApp, navint-tests, e2e-tests), not in: $names"
            }
        }
    }

    Given("CircuitProviders placement") {
        Then("CircuitProviders should only exist in core:circuit") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .filter { it.name == "CircuitProviders" }
                .filter { !it.resideInPath("..core/circuit..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "CircuitProviders must only exist in core:circuit, found in: $names"
            }
        }
    }

    Given("AppGraph interface placement") {
        Then("AppGraph interface should only exist in core:metro") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .filter { it.name == "AppGraph" }
                .filter { !it.resideInPath("..core/metro..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "AppGraph interface must only exist in core:metro, found in: $names"
            }
        }
    }
})
