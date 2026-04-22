plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.mockdonalds.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mockdonalds.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    // Symmetric with iOS xcconfigs: 5 markets × 3 envs × debug/release = 30 Build Variants.
    // Market flavor drives applicationId suffix; env flavor is config-only (values flow to
    // BuildKonfig via task-name parsing in core/build-config/impl/build.gradle.kts).
    flavorDimensions += listOf("market", "env")
    productFlavors {
        create("us") { dimension = "market"; applicationIdSuffix = ".us" }
        create("ca") { dimension = "market"; applicationIdSuffix = ".ca" }
        create("de") { dimension = "market"; applicationIdSuffix = ".de" }
        create("au") { dimension = "market"; applicationIdSuffix = ".au" }
        create("core") { dimension = "market"; applicationIdSuffix = ".core" }
        create("int") { dimension = "env" }
        create("mte") { dimension = "env" }
        create("prod") { dimension = "env" }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("benchmark") {
            // Release-like code (R8 + resource shrinking + proguard) so macrobenchmarks
            // measure production-representative performance. Must NOT be debuggable —
            // androidx.benchmark refuses debuggable targets because JIT is disabled in
            // that mode, which invalidates measurements. Perfetto access comes from the
            // `<profileable android:shell="true" />` tag in src/benchmark/AndroidManifest.xml.
            // Uses debug signing so CI can install the APK without release keystore access.
            // Never ships — production AAB comes from the `release` build type.
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Required by io.harness:ff-android-client-sdk (see core/feature-flag/AGENTS.md).
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
