plugins {
    id("sampleplatter.kmp.domain")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.circuit"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.circuit.runtime)
            api(libs.circuit.runtime.presenter)
            api(libs.circuit.runtime.ui)
            api(libs.circuit.foundation)
            implementation(compose.runtime)
        }
    }
}
