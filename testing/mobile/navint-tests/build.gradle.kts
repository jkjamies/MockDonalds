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
            api(project(":core:build-config:test"))
            api(project(":core:logger:test"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:test-fixtures"))

            // Consumer feature modules — real presenters + UI, fake data layer.
            // Walks `features/{mobile,shared}/`. Skips impl/data + impl/domain (the
            // navint graph wires fakes from test/ as the sole bindings instead).
            // Kiosk navint coverage lives in `:testing:kiosk:navint-tests`.
            listOf("mobile", "shared").forEach { grouping ->
                rootDir.resolve("features/$grouping").walkTopDown()
                    .filter { dir -> dir.isDirectory && dir.resolve("build.gradle.kts").exists() }
                    .forEach { module ->
                        val rel = module.relativeTo(rootDir).path
                        if (rel.endsWith("/impl/data") || rel.endsWith("/impl/domain")) return@forEach
                        api(project(":" + rel.replace("/", ":")))
                    }
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
