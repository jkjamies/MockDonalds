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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    // Compose UI testing
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.androidx.test.runner)

    // Benchmark
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.uiautomator)

    // TestTags from feature api/navigation modules (shared accessibility identifiers)
    val features = rootDir.resolve("features").listFiles()
        ?.filter { it.isDirectory }
        ?.map { it.name }
        ?: emptyList()

    features.forEach { feature ->
        implementation(project(":features:$feature:api:navigation"))
    }
}
