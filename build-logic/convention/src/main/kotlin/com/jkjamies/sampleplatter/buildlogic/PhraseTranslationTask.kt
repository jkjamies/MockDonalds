package com.jkjamies.sampleplatter.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Pulls translations from a Phrase project and writes them as Android `strings.xml`
 * (per-locale `values{-locale}/strings.xml`) plus iOS `.lproj/Localizable.strings`,
 * driven by the `sampleplatter.phrase` convention plugin.
 *
 * The typed `@Input` and `@OutputDirectory` properties let Gradle skip re-execution
 * when nothing changed inside a workspace (up-to-date checks). They do NOT opt the
 * task into Gradle's build cache — that requires `@CacheableTask` (or
 * `outputs.cacheIf { … }`). Add that annotation once the Phrase API call is wired
 * and the output is deterministic for a given input set.
 */
abstract class PhraseTranslationTask : DefaultTask() {

    init {
        group = "phrase"
        description = "Pulls translations from Phrase and writes Android strings.xml + iOS Localizable.strings."
    }

    @get:Input
    @get:Optional
    abstract val projectId: Property<String>

    @get:Input
    @get:Optional
    abstract val market: Property<String>

    @get:Input
    @get:Optional
    abstract val locales: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val apiToken: Property<String>

    @get:OutputDirectory
    abstract val androidResDir: DirectoryProperty

    @get:OutputDirectory
    abstract val iosResDir: DirectoryProperty

    @TaskAction
    fun pullTranslations() {
        val token = apiToken.orNull?.takeIf { it.isNotBlank() }
            ?: throw GradleException(
                "Phrase API token missing. Set PHRASE_API_TOKEN environment variable " +
                    "or phrase.apiToken in local.properties.",
            )
        val project = projectId.orNull?.takeIf { it.isNotBlank() }
            ?: throw GradleException(
                "Phrase project ID missing. Set phrase.projectId in local.properties.",
            )

        // Skeleton — wire the Phrase API call before running. Real implementation will:
        //   1. GET /api/v2/projects/{project}/locales — list locales for the active market
        //   2. For each locale, download Android XML and iOS .strings in parallel
        //   3. Write Android XML to androidResDir/values{-locale}/strings.xml
        //   4. Write iOS .strings to iosResDir/{locale}.lproj/Localizable.strings
        @Suppress("UNUSED_VARIABLE") val unusedToken = token
        throw GradleException(
            "PhraseTranslationTask is not yet wired to the Phrase API " +
                "(project=$project, market=${market.getOrElse("default")}).",
        )
    }
}
