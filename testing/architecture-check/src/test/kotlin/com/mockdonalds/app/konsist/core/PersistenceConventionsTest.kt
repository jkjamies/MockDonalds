package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the SQLDelight ownership boundary.
 *
 * MockDonalds uses the Slack / Cash App pattern: a single application-wide
 * `AppDatabase` aggregated in `composeApp`, with each feature contributing its
 * own `.sq` schemas inside `features/{name}/impl/data/sqldelight/`. The umbrella
 * `core:persistence` module owns shared infrastructure (`DatabaseDriverFactory`
 * and the per-platform driver actuals). Nothing else may touch SQLDelight runtime
 * APIs — presentation must not query SQLDelight DAOs directly, domain stays pure,
 * and core modules outside `core:persistence` must not import SQLDelight runtime.
 */
class PersistenceConventionsTest : BehaviorSpec({

    val sqldelightAllowedPathFragments = listOf(
        "/core/persistence/api/",
        "/core/persistence/impl/",
        "/core/persistence/test/",
        "/composeApp/",
    )

    fun isInFeatureDataModule(path: String): Boolean =
        path.contains("/features/") && path.contains("/impl/data/")

    fun isSqlDelightAllowedConsumer(path: String): Boolean =
        sqldelightAllowedPathFragments.any { path.contains(it) } || isInFeatureDataModule(path)

    Given("the SQLDelight import boundary") {
        val files = Konsist.scopeFromProject().files

        Then("only :core:persistence, :composeApp, and feature data modules import app.cash.sqldelight.*") {
            val violators = files.filter { file ->
                !isSqlDelightAllowedConsumer(file.path) && file.imports.any {
                    it.name.startsWith("app.cash.sqldelight.")
                }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "SQLDelight (app.cash.sqldelight.*) is restricted to core:persistence, the " +
                    "composeApp aggregator, and per-feature data modules. Move database access " +
                    "into features/{name}/impl/data/ and expose results through repository " +
                    "interfaces:\n$names"
            }
        }
    }
})
