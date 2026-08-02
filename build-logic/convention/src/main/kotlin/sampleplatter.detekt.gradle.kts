plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.absolutePath.contains("/build/") }

    // `exclude` filters what detekt *analyses*, but the KSP output directory
    // (`build/generated/ksp/metadata/commonMain/kotlin`) is still part of the Kotlin source
    // set, so Gradle sees this task consuming another task's output. With no ordering rule
    // declared, Gradle fails validation the moment detekt and a KSP-consuming task land in
    // the same invocation — which is exactly what `verify full` does:
    //
    //   Task ':core:build-config:api:detektMetadataCommonMain' uses this output of task
    //   ':core:build-config:api:kspCommonMainKotlinMetadata' without declaring [...] a dependency
    //
    // Running task-by-task (as CI does) hides it, because the two never share a task graph.
    // `mustRunAfter` rather than `dependsOn`: detekt does not need KSP's output produced, it
    // only must not race it — `dependsOn` would force codegen on every lint run.
    mustRunAfter(tasks.matching { it.name.startsWith("ksp") })
}

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "detektPlugins"(catalog.findLibrary("detekt-formatting").get())
}
