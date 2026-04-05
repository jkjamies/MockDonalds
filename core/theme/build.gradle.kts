plugins {
    id("mockdonalds.kmp.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.theme"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}
