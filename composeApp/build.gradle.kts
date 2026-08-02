plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kmp.nativecoroutines)
    alias(libs.plugins.sqldelight)
}

metro {
    enableCircuitCodegen.set(true)
}

// SQLDelight schema declaration is owned by the contributing feature module —
// `features/order/impl/data` applies the `app.cash.sqldelight` plugin and declares
// the application-wide `AppDatabase` (packageName = "com.jkjamies.sampleplatter.persistence").
// composeApp imports the generated class via its existing `implementation(project(...))`
// dependency on the feature and instantiates `AppDatabase(driver)` in `DatabaseProviders`.
//
// When a SECOND feature starts contributing tables, switch to the SQLDelight cross-module
// aggregation pattern: have composeApp re-declare `databases { create("AppDatabase") }`
// with `dependency(project(...))` for each contributing feature, and add
// `evaluationDependsOnChildren()` in settings.gradle.kts so contributor SQLDelight
// plugins are applied before composeApp's `dependency()` call resolves.

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.composeapp"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            // Export feature modules for iOS consumption (auto-discovered).
            //
            // Everything exported becomes a public Obj-C symbol AND a dead-code-elimination
            // root, so the export list is the primary lever on framework size. Only export
            // what SwiftUI genuinely names:
            //   api:domain        — models read off UiState (CategoryPreview, RecentItem, …)
            //   api:navigation    — Screen objects and TestTags for @CircuitInject / robots
            //   impl:presentation — {Feature}UiState and {Feature}Event
            // Everything else reaches Swift by reachability from `IosApp`'s public API and
            // does not need exporting — see `core:network:api`/`AkamaiSensorBridge`.
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    export(project(":features:$feature:api:domain"))
                    export(project(":features:$feature:api:navigation"))
                    export(project(":features:$feature:impl:presentation"))
                }
            export(project(":core:circuit"))
            // Both `:api` and `:impl` — and `:impl` is NOT removable, despite only one Swift
            // symbol needing it. Do not "optimise" this without reading the next paragraph.
            //
            // `:api` carries the HarnessIosBridge contract that Swift implements. `:impl`
            // carries `RemoteConfigBuildConfig`, a BuildKonfig-generated object (declared in
            // core/remote-config/impl/build.gradle.kts under the *api* package name, so it does
            // not appear in any source-file scan). `SwiftHarnessBridge` reads
            // `RemoteConfigBuildConfig.shared.HARNESS_CLIENT_ID` directly, because AppDelegate
            // constructs it BEFORE `IosApp` exists — it is the thing being passed in, so it
            // cannot resolve the key through the DI graph.
            //
            // Dropping `:impl` here compiles and links on the Kotlin side and fails only at
            // Swift compile with "cannot find 'RemoteConfigBuildConfig' in scope". Moving the
            // BuildKonfig block to `:api` would fix it but would put the BuildKonfig plugin on
            // the classpath of every consumer of remote-config:api — which is core:presentation,
            // and therefore every feature presentation module — contradicting the rule in
            // .agents/standards/build-config.md that the generated object and the plugin stay
            // out of feature classpaths.
            export(project(":core:remote-config:api"))
            export(project(":core:remote-config:impl"))
            export(project(":core:build-config:api"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose RUNTIME only — see androidMain below for Compose UI.
            //
            // iOS runs presenters through the Compose runtime via Molecule and renders with
            // SwiftUI; it never touches Compose UI. Anything declared here is compiled for
            // iosX64/iosArm64/iosSimulatorArm64 on every build, so Compose UI in commonMain
            // is pure cost for the iOS framework. commonMain in this module has zero
            // `androidx.compose` imports of any kind; only iosMain needs the runtime
            // (CircuitPresenterKotlinBridge uses Composable/currentComposer).
            implementation(compose.runtime)

            // Feature modules (auto-discovered, architecture-enforced wiring).
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:$feature:api:domain"))
                    api(project(":features:$feature:api:navigation"))
                    implementation(project(":features:$feature:impl:data"))
                    implementation(project(":features:$feature:impl:domain"))
                    api(project(":features:$feature:impl:presentation"))
                }

            // Core
            // `api` here is not a style choice: Kotlin/Native requires every `export`ed
            // project to also be an api-dependency of the source set. Anything NOT exported
            // stays `implementation` so it does not leak onto consumers' compile classpath.
            api(project(":core:circuit"))
            api(project(":core:remote-config:api"))
            // Exported, so it must be declared api() directly. It previously reached the api
            // configuration only transitively through `implementation(:core:build-config:impl)`,
            // which is not what Kotlin/Native's exported-dependency check reads.
            api(project(":core:build-config:api"))
            // `implementation`, not `api`: core:metro is internal DI wiring (the AppGraph
            // contract) and is not exported. As `api` it pushed core:metro plus its five
            // transitive api deps onto androidApp's compile classpath for no reason.
            implementation(project(":core:metro"))
            implementation(project(":core:analytics:impl"))
            implementation(project(":core:auth:impl"))
            // `api`, not `implementation`: Kotlin/Native requires every exported project to be
            // an api-dependency of the source set, and this one is exported for
            // RemoteConfigBuildConfig — see the export block above.
            api(project(":core:remote-config:impl"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:network:impl"))
            implementation(project(":core:build-config:impl"))
            implementation(project(":core:persistence:impl"))
            implementation(project(":core:logger:impl"))

            // Circuit
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.circuitx.gesture.navigation)

            // Metro DI
            implementation(libs.metro.runtime)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(project(":core:analytics:test"))
            implementation(project(":core:test-fixtures"))
            implementation(libs.circuit.test)
        }

        androidMain.dependencies {
            // Compose UI is Android-only by design — App.kt, SamplePlatterBottomNavigation.kt
            // and SamplePlatterIcons.kt are the only Compose UI consumers in this module.
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.compose.material3.windowsizeclass)
        }

        iosMain.dependencies {
            // Molecule for Compose → StateFlow bridge
            implementation(libs.molecule.runtime)
            implementation(libs.nativecoroutines.core)
        }
    }
}

