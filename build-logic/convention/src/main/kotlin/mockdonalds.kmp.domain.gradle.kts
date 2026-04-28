plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")
}

if (project.path != ":core:logger:impl") {
    kotlin.sourceSets.getByName("commonMain") {
        dependencies {
            implementation(project(":core:logger:api"))
        }
    }
}
