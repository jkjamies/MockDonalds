plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.mockdonalds.app.e2e"
    compileSdk = 36

    targetProjectPath = ":androidApp"

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("market", "core")
        missingDimensionStrategy("env", "int")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    // Compose UI testing — drives the app via accessibility identifiers and injects
    // AppComponentFactoryRegistry into the target process. Safe here because e2e-tests
    // runs against the target's debug variant (unminified). Macrobenchmarks live in
    // :testing:benchmarks which targets the minified benchmark variant.
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.uiautomator)

    // TestTags from consumer feature api/navigation modules (shared accessibility identifiers).
    // Walks `features/mobile/{name}/` plus `features/shared/{name}/` — only includes
    // features that have an api/navigation submodule (e.g., shared/menu has none — its
    // Screen lives in mobile/order's api/navigation). Kiosk e2e lives at
    // `:testing:kiosk:e2e-tests`.
    listOf("mobile", "shared").forEach { grouping ->
        rootDir.resolve("features/$grouping").listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted()
            ?.forEach { feature ->
                if (rootDir.resolve("features/$grouping/$feature/api/navigation/build.gradle.kts").exists()) {
                    implementation(project(":features:$grouping:$feature:api:navigation"))
                }
            }
    }
}
