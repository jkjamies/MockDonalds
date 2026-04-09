plugins {
    id("mockdonalds.kmp.domain")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.centerpost"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(compose.runtime)
        }
        commonTest.dependencies {
        }
    }
}
