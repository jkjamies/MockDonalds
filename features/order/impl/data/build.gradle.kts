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

// Feature-owned schema slot for the application-wide AppDatabase.
// composeApp is the aggregator: it pulls this module via `dependency(...)` inside its
// `create("AppDatabase")` block and exposes the merged AppDatabase + per-feature Queries
// classes through Metro DI. Mirrors the Slack / Cash App pattern.
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.mockdonalds.app.persistence")
        }
    }
}
