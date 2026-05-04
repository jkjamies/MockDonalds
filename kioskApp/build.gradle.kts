plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.mockdonalds.kiosk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mockdonalds.kiosk"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    // Symmetric with androidApp + iOS xcconfigs: 5 markets × 3 envs × debug/release = 30 Build Variants.
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
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        jniLibs {
            pickFirsts += listOf(
                "lib/arm64-v8a/libakamai.so",
                "lib/armeabi-v7a/libakamai.so",
                "lib/x86/libakamai.so",
                "lib/x86_64/libakamai.so",
            )
        }
    }
}

dependencies {
    implementation(project(":kioskComposeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
