plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlin.multiplatform.toDep())
    compileOnly(libs.plugins.android.kotlin.multiplatform.library.toDep())
    compileOnly(libs.plugins.compose.multiplatform.toDep())
    compileOnly(libs.plugins.compose.compiler.toDep())
    compileOnly(libs.plugins.kotlin.parcelize.toDep())
    compileOnly(libs.plugins.kotlin.serialization.toDep())
    compileOnly(libs.plugins.ksp.toDep())
    compileOnly(libs.plugins.metro.toDep())
    implementation(libs.plugins.kotest.toDep())
    implementation(libs.plugins.detekt.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
