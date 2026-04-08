package com.mockdonalds.app.konsist.layers

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates the api layer conventions:
 * - Domain models (data classes) live in api modules
 * - Data classes should be immutable (val only)
 * - Serialization annotations only in api or data layers
 */
class ApiLayerTest : BehaviorSpec({

    Given("model immutability") {
        Then("data classes in api modules should only have val properties") {
            Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasDataModifier &&
                        it.resideInPath("..api..") &&
                        it.resideInPath("..commonMain..")
                }
                .assertTrue(additionalMessage = "Domain models must be immutable — use val, not var") {
                    it.properties().none { prop -> prop.hasVarModifier }
                }
        }
    }

    Given("serialization placement") {
        Then("@Serializable should only appear in api or data modules") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.hasAnnotation { a -> a.name == "Serializable" } }
                .filter {
                    !it.resideInPath("..api..") &&
                        !it.resideInPath("..data..") &&
                        !it.resideInPath("..network..")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "@Serializable classes should only live in api, data, or network modules:\n$names"
            }
        }
    }

    Given("no mutable flows in public APIs") {
        Then("public properties should not expose MutableStateFlow or MutableSharedFlow") {
            val violators = Konsist.scopeFromProject()
                .properties()
                .filter {
                    it.resideInPath("..commonMain..") &&
                        (it.resideInPath("..api..") || it.resideInPath("..domain..")) &&
                        !it.hasPrivateModifier &&
                        !it.hasProtectedModifier
                }
                .filter { prop ->
                    val type = prop.type?.name ?: ""
                    type.contains("MutableStateFlow") || type.contains("MutableSharedFlow")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name}: ${it.type?.name} (${it.path})" }
                "Public APIs must not expose MutableStateFlow or MutableSharedFlow — use Flow instead:\n$names"
            }
        }
    }

    Given("api module exports") {
        Then("all Screen files in api navigation should import circuit.runtime") {
            val screenFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..api..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("Screen.kt")
                }

            screenFiles.forEach { file ->
                val hasCircuitImport = file.imports.any {
                    it.name.startsWith("com.slack.circuit.runtime")
                }
                assert(hasCircuitImport) {
                    "Screen file '${file.name}' in api:navigation must import from com.slack.circuit.runtime"
                }
            }
        }
    }

    Given("api:domain isolation") {
        Then("files in api:domain must not import circuit.runtime") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..api/domain..") &&
                        it.resideInPath("..commonMain..")
                }
                .filter { file ->
                    file.imports.any { it.name.startsWith("com.slack.circuit") }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "api:domain must have no Circuit dependency — move Circuit-dependent code to api:navigation:\n$names"
            }
        }

        Then("Screen files must reside in api:navigation, not api:domain") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..api/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("Screen.kt")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Screen files belong in api:navigation, not api:domain:\n$names"
            }
        }

        Then("TestTags files must reside in api:navigation, not api:domain") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..api/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("TestTags.kt")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "TestTags files belong in api:navigation, not api:domain:\n$names"
            }
        }
    }

    Given("data transfer objects") {
        Then("DTO classes should only reside in data modules") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.name.endsWith("Dto") || it.name.endsWith("DTO") || it.name.endsWith("Response") || it.name.endsWith("Request") }
                .filter {
                    !it.resideInPath("..data..") &&
                        !it.resideInPath("..network..")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "DTO/Response/Request classes should only live in data or network modules:\n$names"
            }
        }
    }
})
