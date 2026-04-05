import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi

plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
}

@OptIn(ExperimentalMetroGradleApi::class)
metro {
    enableCircuitCodegen.set(true)
    generateContributionHints.set(true)
}
