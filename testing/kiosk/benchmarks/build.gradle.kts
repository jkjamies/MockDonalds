plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.mockdonalds.kiosk.benchmarks"
    compileSdk = 36

    targetProjectPath = ":kioskApp"

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("market", "core")
        missingDimensionStrategy("env", "int")
    }

    // Macrobenchmarks run against the kiosk app's `benchmark` build type so
    // startup/frame timings reflect production-representative R8-minified code.
    buildTypes {
        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    experimentalProperties["android.experimental.self-instrumenting"] = true
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType != "benchmark") {
            variantBuilder.enable = false
        }
    }
}

dependencies {
    // Macrobenchmark + UiAutomator only — no compose.ui.test.junit4 here.
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
}
