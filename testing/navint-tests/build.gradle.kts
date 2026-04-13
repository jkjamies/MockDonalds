plugins {
    id("mockdonalds.kmp.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
}

metro {
    enableCircuitCodegen.set(true)
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.navint"

        withDeviceTest {
            instrumentationRunner = "com.mockdonalds.app.navint.TestRunner"
            packaging {
                resources.excludes.addAll(
                    listOf(
                        "META-INF/AL2.0",
                        "META-INF/LGPL2.1",
                        "META-INF/LICENSE.md",
                        "META-INF/LICENSE-notice.md",
                    )
                )
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core
            api(project(":core:metro"))
            api(project(":core:circuit"))
            api(project(":core:analytics:test"))
            api(project(":core:auth:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:test-fixtures"))

            // Feature modules — real presenters + UI, fake data layer
            // Auto-discover all features, same as composeApp but with test/ instead of impl/domain + impl/data
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:$feature:api:domain"))
                    api(project(":features:$feature:api:navigation"))
                    api(project(":features:$feature:impl:presentation"))  // real presenters + UI
                    api(project(":features:$feature:test"))               // fakes as sole bindings
                }

            // Circuit
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.circuitx.gesture.navigation)

            // Metro DI
            implementation(libs.metro.runtime)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}

kotlin.sourceSets.getByName("androidDeviceTest") {
    dependencies {
        implementation(libs.compose.ui.test.junit4)
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.compose.material3.windowsizeclass)
    }
}

// Compose Multiplatform 1.10.3 doesn't configure outputDirectory for androidDeviceTest resource copy task
tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.configureEach {
    enabled = false
}
