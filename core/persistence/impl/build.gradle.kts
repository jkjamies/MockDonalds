plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.persistence.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:persistence:api"))
            implementation(libs.sqldelight.runtime)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}
