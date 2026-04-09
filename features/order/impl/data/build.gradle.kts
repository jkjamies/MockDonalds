plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:impl:domain"))
            implementation(project(":core:network"))
        }
    }
}
