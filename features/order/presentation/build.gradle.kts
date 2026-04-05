plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api"))
            implementation(project(":features:order:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
