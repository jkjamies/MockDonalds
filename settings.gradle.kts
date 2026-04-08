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
include(":core:common")

// Feature modules — auto-discovered, architecture-enforced submodules
rootDir.resolve("features").listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature ->
        include(":features:$feature:api:domain")
        include(":features:$feature:api:navigation")
        include(":features:$feature:data")
        include(":features:$feature:domain")
        include(":features:$feature:presentation")
        include(":features:$feature:test")
    }

// Architecture tests
include(":konsist")
