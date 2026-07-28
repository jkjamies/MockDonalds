package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.mockdonalds.app.konsist.normalizedPath
import com.mockdonalds.app.konsist.projectRootFrom
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates Android UI test conventions:
 * - Every *Ui.kt in androidMain has a corresponding *UiTest in androidDeviceTest
 * - Every *UiTest has a corresponding *UiRobot and *StateRobot
 * - UiRobot classes own a StateRobot (composition, not inheritance)
 * - UiRobot setContent calls wrap in SamplePlatterTheme
 * - TestTags objects live alongside their Ui composable
 * - StateRobots extend the shared StateRobot base class
 */
class UiTestConventionsTest : BehaviorSpec({

    Given("UI test coverage") {
        Then("every *Ui.kt in androidMain should have a *UiTest in androidDeviceTest") {
            val uiFiles = Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" } &&
                        it.resideInPath("..androidMain..")
                }
                .filter { it.name.endsWith("Ui") }
                .map { it.name }
                .toSet()

            val uiTestClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..androidDeviceTest..") }
                .withNameEndingWith("UiTest")
                .map { it.name.removeSuffix("Test") }
                .toSet()

            val untested = uiFiles.filter { it !in uiTestClasses }

            assert(untested.isEmpty()) {
                "UI composables missing androidDeviceTest tests:\n${untested.joinToString("\n") { "  $it — expected ${it}Test" }}"
            }
        }
    }

    Given("robot pattern") {
        Then("every *UiTest should have a corresponding *UiRobot") {
            val uiTestClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..androidDeviceTest..") }
                .withNameEndingWith("UiTest")
                .map { it.name.removeSuffix("Test") }
                .toSet()

            val uiRobotClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..androidDeviceTest..") }
                .withNameEndingWith("UiRobot")
                .map { it.name.removeSuffix("Robot") }
                .toSet()

            val missingRobots = uiTestClasses.filter { it !in uiRobotClasses }

            assert(missingRobots.isEmpty()) {
                "UiTests missing corresponding UiRobot:\n${missingRobots.joinToString("\n") { "  ${it}Test — expected ${it}Robot" }}"
            }
        }

        Then("every *UiRobot should have a corresponding *StateRobot") {
            val uiRobotClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..androidDeviceTest..") }
                .withNameEndingWith("UiRobot")
                .map { it.name.removeSuffix("UiRobot") }
                .toSet()

            val stateRobotClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..androidDeviceTest..") }
                .withNameEndingWith("StateRobot")
                .map { it.name.removeSuffix("StateRobot") }
                .toSet()

            val missingStateRobots = uiRobotClasses.filter { it !in stateRobotClasses }

            assert(missingStateRobots.isEmpty()) {
                "UiRobots missing corresponding StateRobot:\n${missingStateRobots.joinToString("\n") { "  ${it}UiRobot — expected ${it}StateRobot" }}"
            }
        }

        Then("StateRobots should extend the shared StateRobot base class") {
            val stateRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("StateRobot.kt") }

            val violators = stateRobotFiles.filter { file ->
                !file.imports.any { it.name == "com.mockdonalds.app.core.test.StateRobot" }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "StateRobots must extend StateRobot from core:test-fixtures:\n$names"
            }
        }
    }

    Given("robot encapsulation") {
        Then("UiTest files should only reference UiRobot, not StateRobot directly") {
            val uiTestFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiTest.kt") }

            val violators = uiTestFiles.filter { file ->
                file.text.contains("StateRobot")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiTest must only use UiRobot — StateRobot is an implementation detail of UiRobot:\n$names"
            }
        }
    }

    Given("test tags") {
        // Pair every screen with the feature module that owns it. Screen name alone is not a
        // usable key: WelcomeUi lives in :features:login and CategoryDetailUi in :features:order,
        // so deriving the module from the name would look for :features:welcome and
        // :features:categorydetail, neither of which exists. Comparing bare names also let a
        // TestTags object in any module satisfy a Ui composable in a different one.
        fun featureOf(path: String): String? =
            Regex("""/features/([^/]+)/""").find(normalizedPath(path))?.groupValues?.get(1)

        Then("every *Ui.kt in androidMain should have a *TestTags object in its own feature") {
            val uiKeys = Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" } &&
                        it.resideInPath("..androidMain..")
                }
                .filter { it.name.endsWith("Ui") }
                .mapNotNull { fn -> featureOf(fn.path)?.let { it to fn.name.removeSuffix("Ui") } }
                .toSet()

            val tagKeys = Konsist.scopeFromProject()
                .objects()
                .filter { it.resideInPath("..api/navigation..") }
                .filter { it.name.endsWith("TestTags") }
                .mapNotNull { obj ->
                    featureOf(obj.path)?.let { it to obj.name.removeSuffix("TestTags") }
                }
                .toSet()

            val missing = uiKeys.filterNot { it in tagKeys }

            assert(missing.isEmpty()) {
                val names = missing.joinToString("\n") { (feature, screen) ->
                    "  ${screen}Ui — expected ${screen}TestTags in :features:$feature:api:navigation"
                }
                "UI composables missing a TestTags object in their own feature's " +
                    "api:navigation module:\n$names"
            }
        }

        Then("TestTags objects should reside in the api:navigation module's ui package") {
            // The message below has always promised the `ui` package, but the check only tested
            // for "api" anywhere in the path — so the package half went unenforced, and any
            // directory containing "api" satisfied the module half.
            val violators = Konsist.scopeFromProject()
                .objects()
                .filter { it.name.endsWith("TestTags") }
                .filter { obj ->
                    !obj.resideInPath("..api/navigation..") ||
                        obj.packagee?.name?.endsWith(".api.ui") != true
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") {
                    "  ${it.name} (${it.packagee?.name ?: "no package"} — ${it.path})"
                }
                "TestTags objects must live in the feature api:navigation module, in its " +
                    "`ui` package:\n$names"
            }
        }
    }

    Given("theme wrapping") {
        Then("UiRobot setContent calls should wrap in SamplePlatterTheme") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiRobot.kt") }

            val violators = uiRobotFiles.filter { file ->
                val text = file.text
                text.contains("setContent") && !text.contains("SamplePlatterTheme")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiRobot setContent calls must wrap content in SamplePlatterTheme for accurate rendering:\n$names"
            }
        }
    }

    Given("landscape testing") {
        Then("every *UiRobot should have a setLandscapeContent method") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiRobot.kt") }

            val violators = uiRobotFiles.filter { file ->
                !file.text.contains("setLandscapeContent")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiRobots must have a setLandscapeContent() method for landscape testing:\n$names"
            }
        }

        Then("every *UiRobot should have an assertLandscapeScreen method") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiRobot.kt") }

            val violators = uiRobotFiles.filter { file ->
                !file.text.contains("assertLandscapeScreen")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiRobots must have an assertLandscapeScreen() method for landscape testing:\n$names"
            }
        }

        Then("every *UiTest should have a rendersLandscapeLayout test") {
            val uiTestFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiTest.kt") }

            val violators = uiTestFiles.filter { file ->
                !file.text.contains("rendersLandscapeLayout")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiTests must have a rendersLandscapeLayout test for landscape coverage:\n$names"
            }
        }
    }

    Given("WindowSizeClass provision") {
        Then("every *UiRobot should provide LocalWindowSizeClass") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.nameWithExtension.endsWith("UiRobot.kt") }

            val violators = uiRobotFiles.filter { file ->
                !file.text.contains("LocalWindowSizeClass")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiRobots must provide LocalWindowSizeClass via CompositionLocalProvider for landscape support:\n$names"
            }
        }
    }

    Given("androidDeviceTest manifest") {
        Then("every presentation module with UI tests should have an AndroidManifest.xml declaring ComponentActivity") {
            val projectRoot = projectRootFrom(Konsist.scopeFromProject().files.first().path)

            val featuresDir = projectRoot.resolve("features")
            val features = featuresDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?: error("Could not enumerate $featuresDir")

            assert(features.isNotEmpty()) {
                "No feature modules found under $featuresDir — this check must not pass by inspecting zero modules"
            }

            // Presentation modules are nested under impl/. This probed
            // features/<name>/presentation/... — one segment short — so the filter matched nothing
            // and every manifest went unexamined regardless of its contents.
            val presentationModules = features.filter {
                featuresDir.resolve("$it/impl/presentation/src/androidDeviceTest/kotlin").exists()
            }

            // Both empties are indistinguishable in the assert below, so rule out the broken one
            // here. Every feature in this repo ships androidDeviceTest sources; if that ever stops
            // being true, narrow this guard rather than dropping it.
            assert(presentationModules.isNotEmpty()) {
                "No presentation modules with androidDeviceTest sources found under $featuresDir — " +
                    "the layout this check probes has moved"
            }

            val missingManifest = presentationModules.filter { feature ->
                val manifest =
                    featuresDir.resolve("$feature/impl/presentation/src/androidDeviceTest/AndroidManifest.xml")
                !manifest.exists() || !manifest.readText().contains("ComponentActivity")
            }

            assert(missingManifest.isEmpty()) {
                "Presentation modules with androidDeviceTest missing AndroidManifest.xml declaring ComponentActivity:\n${missingManifest.joinToString("\n") { "  :features:$it:presentation" }}"
            }
        }
    }
})
