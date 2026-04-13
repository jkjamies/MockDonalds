package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * General code hygiene checks that are architectural in nature
 * (not formatting/style — that's for a linter).
 */
class CodeHygieneTest : BehaviorSpec({

    val productionFiles = Konsist.scopeFromProject()
        .files
        .filter {
            it.resideInPath("..commonMain..") || it.resideInPath("..androidMain..")
        }

    Given("no wildcard imports") {
        Then("production code should not use wildcard imports") {
            val violators = productionFiles
                .flatMap { file ->
                    file.imports
                        .filter { it.isWildcard }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Wildcard imports are not allowed — use explicit imports:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("no debug logging in production") {
        Then("production code should not use println") {
            val violators = productionFiles
                .filter { file ->
                    file.imports.any {
                        it.name == "kotlin.io.println"
                    }
                }

            // Also check for direct println usage in source text (no import needed for top-level println)
            // Exempt core:analytics:impl — logging dispatcher is the shell impl until a real SDK ships
            val textViolators = productionFiles
                .filter { file ->
                    file.text.contains("println(") &&
                        !file.name.endsWith("Test.kt") &&
                        !file.resideInPath("..core/analytics/impl..")
                }

            val all = (violators + textViolators).distinctBy { it.path }

            assert(all.isEmpty()) {
                val names = all.joinToString("\n") { "  ${it.name} (${it.path})" }
                "println is not allowed in production code — use a logging framework:\n$names"
            }
        }

        Then("production code should not use System.out or System.err") {
            val violators = productionFiles
                .filter { file ->
                    file.imports.any {
                        it.name.startsWith("java.lang.System")
                    } || file.text.contains("System.out") || file.text.contains("System.err")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "System.out/System.err are not allowed in production code:\n$names"
            }
        }
    }

    Given("no blocking calls") {
        Then("no Thread.sleep in production or test code") {
            val violators = Konsist.scopeFromProject().files
                .filter { !it.resideInPath("..konsist..") }
                .filter { file ->
                    file.text.contains("""Thread.sleep""")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Thread.sleep is not allowed anywhere — use coroutine delay instead:\n$names"
            }
        }

        Then("no runBlocking in production code") {
            val violators = productionFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.runBlocking" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "runBlocking is not allowed in production code — use suspend functions or CenterPost:\n$names"
            }
        }
    }

    Given("no force unwraps in production") {
        Then("production code should not use non-null assertion operator (!!)") {
            val violators = productionFiles
                .filter { it.text.contains("!!") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Non-null assertion (!!) is not allowed in production code — handle nullability explicitly:\n$names"
            }
        }
    }

    Given("no lateinit var in production") {
        Then("production classes should not use lateinit var") {
            val violators = Konsist.scopeFromProject()
                .properties()
                .filter {
                    it.hasLateinitModifier &&
                        (it.resideInPath("..commonMain..") || it.resideInPath("..androidMain..")) &&
                        !it.resideInPath("..androidApp..") &&
                        !it.resideInPath("..composeApp..")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "lateinit var is not allowed in shared production code — use constructor injection or lazy:\n$names"
            }
        }
    }
})
