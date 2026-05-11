plugins {
    id("mockdonalds.kmp.data")
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
            implementation(project(":core:centerpost"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
        }
    }
}

// Feature-owned AppDatabase declaration. This module currently OWNS the AppDatabase
// (it declares `create("AppDatabase")` below, generates the schema + Queries here, and
// `composeApp` consumes the resulting class via its existing project dependency on us).
// When a second feature contributes tables, switch to the SQLDelight cross-module
// aggregation pattern: have composeApp re-declare `create("AppDatabase")` with a
// `dependency(project(":features:<name>:impl:data"))` per contributor, and add
// `evaluationDependsOnChildren()` in settings.gradle.kts so contributor plugins are
// applied before composeApp's `dependency(...)` resolves. Mirrors the Slack/Cash App
// pattern at that point.
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.mockdonalds.app.persistence")
        }
    }
}
