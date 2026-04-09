pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MockDonalds"

// App
include(":androidApp")
include(":composeApp")

// Core modules
include(":core:centerpost")
include(":core:test-fixtures")
include(":core:theme")
include(":core:network")
include(":core:circuit")
include(":core:auth:api")
include(":core:auth:impl")
include(":core:metro")

// Feature modules — auto-discovered, architecture-enforced submodules
rootDir.resolve("features").listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature ->
        include(":features:$feature:api:domain")
        include(":features:$feature:api:navigation")
        include(":features:$feature:impl:data")
        include(":features:$feature:impl:domain")
        include(":features:$feature:impl:presentation")
        include(":features:$feature:test")
    }

// Architecture tests
include(":architecture-check")
