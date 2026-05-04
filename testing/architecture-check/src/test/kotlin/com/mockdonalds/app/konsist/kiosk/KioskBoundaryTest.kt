package com.mockdonalds.app.konsist.kiosk

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the absolute static separation between consumer and kiosk surfaces.
 *
 * **Mobile and kiosk never import each other.** All cross-host reuse goes
 * through `features/shared/{name}` — features whose actual screens or data layers
 * are consumed by both apps. The classic example is `features/shared/menu/`,
 * which holds `OrderContent`, `MenuItem`, `GetOrderContent`, `OrderRepository`,
 * etc.; both `features/mobile/order/` and `features/kiosk/order/` consume that
 * shared domain via their own (app-specific) presenters and screens.
 *
 * | From → To             | Allowed? |
 * |-----------------------|----------|
 * | mobile → mobile       | ✓        |
 * | mobile → shared       | ✓        |
 * | mobile → kiosk        | ✗        |
 * | kiosk → kiosk         | ✓        |
 * | kiosk → shared        | ✓        |
 * | kiosk → mobile        | ✗        |
 * | shared → shared       | ✓        |
 * | shared → mobile       | ✗ (would pin shared to consumer specifics)        |
 * | shared → kiosk        | ✗ (would pin shared to kiosk specifics)          |
 *
 * Consumer host (`composeApp`, `androidApp`) and kiosk host (`kioskApp`,
 * `kioskComposeApp`) follow the same rule: each may only consume features
 * within its own grouping plus `features/shared/{name}`.
 *
 * Without this Konsist enforcement, the only safety net would be R8 dead-code
 * stripping — debug-build-fragile, silently masks regressions, easily bypassed
 * by anyone willing to add a manual `implementation(project(...))`.
 *
 * See [`features/AGENTS.md`](../../../../../../../../features/AGENTS.md) for the
 * grouping convention; [`specs/kiosk-app.md`](../../../../../../../../specs/kiosk-app.md)
 * for the kiosk integration spec.
 */
class KioskBoundaryTest : BehaviorSpec({

    Given("the absolute mobile ↔ kiosk import ban") {

        Then("composeApp + androidApp + features/mobile/* must not import features.kiosk.*") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..composeApp..") ||
                        it.resideInPath("..androidApp..") ||
                        it.resideInPath("..features/mobile/..")
                }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("com.mockdonalds.app.features.kiosk.") }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Consumer surfaces must not import any kiosk feature symbol. Cross-host " +
                    "reuse goes through features/shared/* only:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("kioskApp + kioskComposeApp + features/kiosk/* must not import features.mobile.*") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..kioskApp..") ||
                        it.resideInPath("..kioskComposeApp..") ||
                        it.resideInPath("..features/kiosk/..")
                }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("com.mockdonalds.app.features.mobile.") }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Kiosk surfaces must not import any mobile feature symbol. Cross-host " +
                    "reuse goes through features/shared/* only — extract the genuinely " +
                    "shared parts (data layer, use cases) into features/shared/{name}/:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("features/shared/* must not import features.mobile.* or features.kiosk.*") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features/shared/..") }
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name.startsWith("com.mockdonalds.app.features.mobile.") ||
                                it.name.startsWith("com.mockdonalds.app.features.kiosk.")
                        }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "features/shared/* must not depend on either mobile or kiosk specifics — " +
                    "shared features are app-agnostic by definition. If a shared feature " +
                    "needs to know about an app surface, it isn't actually shared:\n" +
                    violators.joinToString("\n")
            }
        }
    }
})
