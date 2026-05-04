package com.mockdonalds.app.konsist.kiosk

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the static separation between consumer surfaces (`androidApp` /
 * `composeApp` / `features/{home,more,rewards,profile,recents,scan,login,
 * debug-menu,nutrition}`) and kiosk surfaces (`kioskApp` / `kioskComposeApp` /
 * `features/kiosk/{attract,identify,order}`).
 *
 * Without these rules the two surfaces would be free to import each other
 * across module boundaries, and the only safety net would be R8 dead-code
 * stripping — which is debug-build-fragile and silently masks regressions.
 *
 * See specs/kiosk-app.md → "Konsist additions — new tests in `:testing:architecture-check`".
 */
class KioskBoundaryTest : BehaviorSpec({

    val consumerOnlyFeatures = setOf(
        "home", "more", "rewards", "profile", "recents", "scan", "login", "debug-menu", "nutrition",
    )

    val consumerOnlyImportPrefixes = consumerOnlyFeatures.map { feature ->
        // debug-menu's package segment is "debugmenu" — kebab → camel-ish per project convention.
        "com.mockdonalds.app.features.${feature.replace("-", "")}"
    }

    Given("the consumer ↔ kiosk module boundary") {

        Then("composeApp must not import any features.kiosk.* symbol") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..composeApp..") }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("com.mockdonalds.app.features.kiosk") }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "composeApp (consumer host) must not import kiosk feature symbols. " +
                    "Move the import to kioskComposeApp instead:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("kioskApp + kioskComposeApp must not import any consumer-only feature symbol") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..kioskApp..") || it.resideInPath("..kioskComposeApp..") }
                .flatMap { file ->
                    file.imports
                        .filter { import ->
                            consumerOnlyImportPrefixes.any { prefix -> import.name.startsWith(prefix) }
                        }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "kiosk host must not import consumer-only feature symbols (home/more/rewards/" +
                    "profile/recents/scan/login/debug-menu/nutrition). Reuse must go through " +
                    "shared core/* modules or features/order/api/* only:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("features/kiosk/* must not import any consumer-only feature symbol") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features/kiosk/..") }
                .flatMap { file ->
                    file.imports
                        .filter { import ->
                            consumerOnlyImportPrefixes.any { prefix -> import.name.startsWith(prefix) }
                        }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "kiosk feature modules must not import consumer-only feature symbols. " +
                    "Reuse must go through core/* or features/order/api/* only:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("kiosk host + kiosk features must not reference any TabScreen implementation") {
            // TabScreen is the consumer bottom-nav primitive. Kiosk has no tab navigation;
            // the visible NavigationRail in KioskOrder is screen-internal Material UI driven
            // by presenter state, not a Circuit-level TabScreen.
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..kioskApp..") ||
                        it.resideInPath("..kioskComposeApp..") ||
                        it.resideInPath("..features/kiosk/..")
                }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.endsWith(".TabScreen") || it.name.contains(".TabScreen") }
                        .map { "  ${file.path}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "kiosk surfaces must not reference TabScreen — kiosk has no tab navigation:\n" +
                    violators.joinToString("\n")
            }
        }
    }
})
