package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces the AppBuildConfig facade rule: consumers must depend on the
 * AppBuildConfig interface (in :core:build-config:api) and receive it via
 * Metro DI. No one outside :core:build-config:impl may import the generated
 * BuildConfig object or the AppBuildConfigImpl class directly.
 */
class BuildConfigImportTest : BehaviorSpec({

    Given("the AppBuildConfig facade boundary") {
        val files = Konsist.scopeFromProject().files

        Then("no file outside :core:build-config:impl imports the generated BuildConfig object") {
            val violators = files.filter { file ->
                val isImplModule = file.path.contains("/core/build-config/impl/")
                !isImplModule && file.imports.any {
                    it.name == "com.jkjamies.sampleplatter.core.buildconfig.BuildConfig"
                }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "BuildConfig is an internal BuildKonfig artifact of :core:build-config:impl. " +
                    "Inject AppBuildConfig (from :core:build-config:api) via Metro instead:\n$names"
            }
        }

        Then("no file outside :core:build-config:impl imports AppBuildConfigImpl") {
            val violators = files.filter { file ->
                val isImplModule = file.path.contains("/core/build-config/impl/")
                !isImplModule && file.imports.any {
                    it.name == "com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfigImpl"
                }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "AppBuildConfigImpl is the Metro-bound production implementation; it is resolved " +
                    "at DI time, never imported. Depend on AppBuildConfig (from :core:build-config:api):\n$names"
            }
        }
    }
})
