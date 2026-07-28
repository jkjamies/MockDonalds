plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.theme"
    }

    sourceSets {
        commonMain.dependencies {
            // The Kotlin design system is Android-only: every consumer of these types is in
            // an `androidMain` or `androidDeviceTest` source set, and iOS renders with its own
            // `iosApp/iosApp/Theme/SamplePlatterTheme.swift`. Keeping MaterialTheme, Typography,
            // Brush, TextStyle and dp/sp in commonMain compiled the whole Compose UI stack for
            // iosX64/iosArm64/iosSimulatorArm64 on every build, for code iOS can never reach —
            // so the sources now live in androidMain.
            //
            // `composeResources/font/` deliberately stays in commonMain: the generated
            // `Res` accessor is emitted into the source set that owns the resources, and
            // androidMain reads it from there. Moving the fonts too would shrink the iOS
            // klib further, but a mistake there fails at runtime (missing font) rather than
            // at compile time, so it is not worth the risk for the remaining bytes.
            implementation(compose.components.resources)
        }
        androidMain.dependencies {
            // compose.runtime (commonMain) and compose.foundation / material3 /
            // materialIconsExtended / ui (androidMain) are supplied by the
            // `mockdonalds.kmp.presentation` convention plugin — do not re-declare them.
            api(libs.androidx.compose.material3.windowsizeclass)
        }
    }
}
