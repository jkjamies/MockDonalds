import com.mockdonalds.buildlogic.PhraseTranslationTask

val phraseProjectId = project.findProperty("phrase.projectId") as String?
val phraseToken = (project.findProperty("phrase.apiToken") as String?)
    ?: System.getenv("PHRASE_API_TOKEN")
val phraseMarket = project.findProperty("market") as String?

tasks.register<PhraseTranslationTask>("pullTranslations") {
    projectId.set(phraseProjectId)
    market.set(phraseMarket ?: "default")
    apiToken.set(phraseToken)
    androidResDir.set(layout.projectDirectory.dir("src/androidMain/res"))
    iosResDir.set(rootProject.layout.projectDirectory.dir("iosApp/iosApp/Resources"))
}
