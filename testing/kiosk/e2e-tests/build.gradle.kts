plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.mockdonalds.kiosk.e2e"
    compileSdk = 36

    targetProjectPath = ":kioskApp"

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
    // Compose UI test + UiAutomator drives the kiosk app via accessibility identifiers.
    // Same shape as :testing:e2e-tests but instruments :kioskApp instead of :androidApp.
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.uiautomator)

    // TestTags from kiosk feature api/navigation modules. Kiosk-only — no consumer
    // feature tags are pulled in here.
    rootDir.resolve("features/kiosk").listFiles()
        ?.filter { it.isDirectory }
        ?.map { it.name }
        ?.sorted()
        ?.forEach { feature ->
            implementation(project(":features:kiosk:$feature:api:navigation"))
        }
}
