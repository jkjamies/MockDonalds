plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.more.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:more:api:domain"))
            api(project(":features:mobile:more:api:navigation"))
            api(project(":core:test-fixtures"))
            api(project(":core:build-config:api"))
            api(project(":core:build-config:test"))
        }
    }
}
