package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces analytics consumption patterns:
 * - Presenters must use TrackAnalyticsEvent interactor, not AnalyticsDispatcher directly
 * - Domain and data layers must use AnalyticsDispatcher, not the TrackAnalyticsEvent interactor
 */
class AnalyticsConventionsTest : BehaviorSpec({

    Given("analytics consumption in presentation layer") {
        Then("presenters should not import AnalyticsDispatcher directly") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/presentation..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = presenterFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.analytics.AnalyticsDispatcher" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Presenters must use TrackAnalyticsEvent interactor, not AnalyticsDispatcher directly:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("analytics consumption in domain layer") {
        Then("domain classes should not import TrackAnalyticsEvent") {
            val domainFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }

            val violators = domainFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.analytics.TrackAnalyticsEvent" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Domain layer must use AnalyticsDispatcher, not the TrackAnalyticsEvent interactor:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("screen view tracking boundaries") {
        Then("domain classes should not call trackScreenView") {
            val domainFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }

            val violators = domainFiles.filter { it.text.contains("trackScreenView") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Screen view tracking is a navigation concern — domain layer must not call trackScreenView:\n$names"
            }
        }

        Then("data classes should not call trackScreenView") {
            val dataFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/data..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = dataFiles.filter { it.text.contains("trackScreenView") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Screen view tracking is a navigation concern — data layer must not call trackScreenView:\n$names"
            }
        }
    }

    Given("analytics consumption in data layer") {
        Then("data classes should not import TrackAnalyticsEvent") {
            val dataFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/data..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = dataFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.analytics.TrackAnalyticsEvent" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Data layer must use AnalyticsDispatcher, not the TrackAnalyticsEvent interactor:\n${violators.joinToString("\n")}"
            }
        }
    }
})
