plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin.sourceSets.getByName("commonMain") {
    dependencies {
        implementation(project(":core:logger:api"))
    }
}
