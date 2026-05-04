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

    // TestTags from kiosk feature api/navigation modules. Kiosk-only — no mobile
    // feature tags pulled in here. Walks `features/kiosk/` for any api/navigation
    // submodule with a build.gradle.kts.
    rootDir.resolve("features/kiosk").walkTopDown()
        .filter { dir -> dir.isDirectory && dir.resolve("build.gradle.kts").exists() }
        .filter { it.relativeTo(rootDir).path.endsWith("/api/navigation") }
        .forEach { implementation(project(":" + it.relativeTo(rootDir).path.replace("/", ":"))) }
}
