plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:order:api:domain"))
            implementation(project(":core:common"))
        }
    }
}
