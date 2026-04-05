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
include(":core:theme")
include(":core:network")
include(":core:common")

// Feature modules
include(":features:home:api")
include(":features:home:data")
include(":features:home:domain")
include(":features:home:presentation")

include(":features:order:api")
include(":features:order:data")
include(":features:order:domain")
include(":features:order:presentation")

include(":features:rewards:api")
include(":features:rewards:data")
include(":features:rewards:domain")
include(":features:rewards:presentation")

include(":features:scan:api")
include(":features:scan:data")
include(":features:scan:domain")
include(":features:scan:presentation")

include(":features:more:api")
include(":features:more:data")
include(":features:more:domain")
include(":features:more:presentation")
