package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.jkjamies.sampleplatter.konsist.isProductionSourcePath
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the api/impl DI placement invariant: all Metro DI annotations
 * (@ContributesBinding, @ContributesTo, @Multibinds, @Provides, @ContributesIntoSet,
 * @DependencyGraph, @SingleIn) live in impl modules. api modules stay annotation-free —
 * they define the public contract (interfaces, abstract use cases, data classes); impl
 * modules do the wiring.
 *
 * See .agents/standards/dependency-injection.md → "DI Annotation Placement — api vs. impl".
 */
class ApiLayerDIAnnotationTest : BehaviorSpec({

    val forbiddenAnnotations = setOf(
        "ContributesBinding",
        "ContributesTo",
        "ContributesIntoSet",
        "ContributesIntoMap",
        "Multibinds",
        "Provides",
        "DependencyGraph",
        "SingleIn",
        "Inject",
    )

    Given("api module DI annotation placement") {
        Then("classes in api modules should not carry Metro DI annotations") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..api..") && isProductionSourcePath(it.path) }
                .flatMap { klass ->
                    klass.annotations
                        .filter { it.name in forbiddenAnnotations }
                        .map { "  ${klass.name} @${it.name} (${klass.path})" }
                }

            assert(violators.isEmpty()) {
                "api modules must stay DI-annotation-free — move DI wiring to impl:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("interfaces in api modules should not carry Metro DI annotations") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .filter { it.resideInPath("..api..") && isProductionSourcePath(it.path) }
                .flatMap { iface ->
                    iface.annotations
                        .filter { it.name in forbiddenAnnotations }
                        .map { "  ${iface.name} @${it.name} (${iface.path})" }
                }

            assert(violators.isEmpty()) {
                "api modules must stay DI-annotation-free — move DI wiring to impl:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("functions in api modules should not carry Metro DI annotations") {
            val violators = Konsist.scopeFromProject()
                .functions()
                .filter { it.resideInPath("..api..") && isProductionSourcePath(it.path) }
                .flatMap { fn ->
                    fn.annotations
                        .filter { it.name in forbiddenAnnotations }
                        .map { "  ${fn.name} @${it.name} (${fn.containingFile.path})" }
                }

            assert(violators.isEmpty()) {
                "api modules must stay DI-annotation-free — move DI wiring to impl:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("files in api modules should not import from dev.zacsweers.metro") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..api..") && isProductionSourcePath(it.path) }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("dev.zacsweers.metro") }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "api modules must not import Metro DI symbols — the contract is DI-agnostic:\n" +
                    violators.joinToString("\n")
            }
        }
    }
})
