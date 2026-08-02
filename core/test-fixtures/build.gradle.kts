plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:auth:api"))
            api(project(":core:strata"))
            api(project(":core:network:api"))
            api(project(":core:logger:api"))
            api(libs.kotlinx.coroutines.test)
            api(libs.kotest.framework.engine)
            api(libs.kotest.assertions.core)
            api(libs.turbine)
            api(libs.kermit.test)
        }
    }
}
