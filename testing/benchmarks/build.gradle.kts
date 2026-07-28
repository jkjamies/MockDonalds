plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.jkjamies.sampleplatter.benchmarks"
    compileSdk = 36

    targetProjectPath = ":androidApp"

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("market", "core")
        missingDimensionStrategy("env", "int")
    }

    // Macrobenchmarks run against the target app's `benchmark` build type so
    // startup/frame timings reflect production-representative R8-minified code.
    // The test APK itself stays unminified so Perfetto traces remain readable.
    buildTypes {
        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Google's canonical macrobenchmark setup: the test APK runs in its own process
    // and observes the target via UiAutomator. Two consequences:
    //   1. `targetContext.packageName` returns the TEST APK's package — benchmarks
    //      must hardcode the target package (see StartupBenchmark.TARGET_PACKAGE).
    //   2. AGP's `checkTestedAppObfuscation` is satisfied because the test APK is
    //      decoupled from the minified target at runtime — no class references cross
    //      APK boundaries, so obfuscation can't break them.
    // See https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

// Only produce benchmark-variant test APKs. Generates `connectedBenchmarkAndroidTest`
// plus market/env-specific variants like `connectedCoreIntBenchmarkAndroidTest`.
androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType != "benchmark") {
            variantBuilder.enable = false
        }
    }
}

dependencies {
    // Intentionally minimal: macrobenchmark + UiAutomator only. Do NOT add
    // compose.ui.test.junit4 here — it injects AppComponentFactoryRegistry into
    // the target process, which calls Kotlin runtime methods that R8 has stripped
    // from the minified target app. That combination crashes the target on launch.
    // Journey tests live in :testing:e2e-tests and run against the debug target.
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
}
