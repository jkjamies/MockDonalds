package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates that fake classes in feature test modules are properly wired for Metro DI.
 * Fakes need @ContributesBinding so navint-tests can auto-discover them via the DI graph.
 */
class TestModuleDITest : BehaviorSpec({

    Given("fake DI wiring in feature test modules") {
        Then("all Fake classes in feature test modules should have @ContributesBinding") {
            Konsist.scopeFromProject()
                .classes()
                .withNameStartingWith("Fake")
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..test/src/commonMain..")
                }
                .assertTrue(additionalMessage = "Fake classes in feature test modules must have @ContributesBinding(AppScope::class) for navint-tests DI auto-discovery") {
                    it.hasAnnotation { a -> a.name == "ContributesBinding" }
                }
        }

        Then("all Fake classes in feature test modules should have @Inject constructor") {
            Konsist.scopeFromProject()
                .classes()
                .withNameStartingWith("Fake")
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..test/src/commonMain..")
                }
                .assertTrue(additionalMessage = "Fake classes in feature test modules must have @Inject constructor for Metro DI") {
                    it.text.contains("@Inject constructor")
                }
        }
    }

    Given("no @ContributesBinding in commonTest") {
        Then("@ContributesBinding should only appear in commonMain source sets") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .filter { it.hasAnnotation { a -> a.name == "ContributesBinding" } }

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "@ContributesBinding found in commonTest — fakes must contribute from commonMain only: $names"
            }
        }
    }
})
