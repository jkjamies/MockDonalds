plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.absolutePath.contains("/build/") }
}

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "detektPlugins"(catalog.findLibrary("detekt-formatting").get())
}
