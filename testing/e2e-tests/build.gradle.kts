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

    // TestTags from feature api/navigation modules (shared accessibility identifiers).
    // Kiosk subdir is excluded — kiosk e2e is a separate suite living alongside its
    // own host once kiosk features land.
    val features = rootDir.resolve("features").listFiles()
        ?.filter { it.isDirectory && it.name != "kiosk" }
        ?.map { it.name }
        ?: emptyList()

    features.forEach { feature ->
        implementation(project(":features:$feature:api:navigation"))
    }
}
