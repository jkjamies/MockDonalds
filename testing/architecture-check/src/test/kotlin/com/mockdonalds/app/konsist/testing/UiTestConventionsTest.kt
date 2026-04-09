package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Validates Android UI test conventions:
 * - Every *Ui.kt in androidMain has a corresponding *UiTest in androidDeviceTest
 * - Every *UiTest has a corresponding *UiRobot and *StateRobot
 * - UiRobot classes own a StateRobot (composition, not inheritance)
 * - UiRobot setContent calls wrap in MockDonaldsTheme
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
                .filter { it.name.endsWith("StateRobot.kt") }

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
                .filter { it.name.endsWith("UiTest.kt") }

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
        Then("every *Ui.kt in androidMain should have a *TestTags object in the api module") {
            val uiFileNames = Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" } &&
                        it.resideInPath("..androidMain..")
                }
                .filter { it.name.endsWith("Ui") }
                .map { it.name.removeSuffix("Ui") }
                .toSet()

            val testTagObjects = Konsist.scopeFromProject()
                .objects()
                .filter { it.resideInPath("..api..") }
                .filter { it.name.endsWith("TestTags") }
                .map { it.name.removeSuffix("TestTags") }
                .toSet()

            val missing = uiFileNames.filter { it !in testTagObjects }

            assert(missing.isEmpty()) {
                "UI composables missing TestTags object in api:navigation module:\n${missing.joinToString("\n") { "  ${it}Ui — expected ${it}TestTags in :features:${it.lowercase()}:api:navigation" }}"
            }
        }

        Then("TestTags objects should reside in the api.ui package") {
            val testTagObjects = Konsist.scopeFromProject()
                .objects()
                .filter { it.name.endsWith("TestTags") }

            val violators = testTagObjects.filter { obj ->
                !obj.resideInPath("..api..")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "TestTags objects must live in the feature api:navigation module (ui package), not in presentation:\n$names"
            }
        }
    }

    Given("theme wrapping") {
        Then("UiRobot setContent calls should wrap in MockDonaldsTheme") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.name.endsWith("UiRobot.kt") }

            val violators = uiRobotFiles.filter { file ->
                val text = file.text
                text.contains("setContent") && !text.contains("MockDonaldsTheme")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UiRobot setContent calls must wrap content in MockDonaldsTheme for accurate rendering:\n$names"
            }
        }
    }

    Given("landscape testing") {
        Then("every *UiRobot should have a setLandscapeContent method") {
            val uiRobotFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..androidDeviceTest..") }
                .filter { it.name.endsWith("UiRobot.kt") }

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
                .filter { it.name.endsWith("UiRobot.kt") }

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
                .filter { it.name.endsWith("UiTest.kt") }

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
                .filter { it.name.endsWith("UiRobot.kt") }

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
            val projectRoot = Konsist.scopeFromProject()
                .files
                .first()
                .path
                .substringBefore("/features/")
                .let { if (it.contains("/core/")) it.substringBefore("/core/") else it }

            val featuresDir = File("$projectRoot/features")
            val presentationModules = featuresDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.filter { File("$projectRoot/features/$it/presentation/src/androidDeviceTest/kotlin").exists() }
                ?: emptyList()

            val missingManifest = presentationModules.filter { feature ->
                val manifest = File("$projectRoot/features/$feature/presentation/src/androidDeviceTest/AndroidManifest.xml")
                !manifest.exists() || !manifest.readText().contains("ComponentActivity")
            }

            assert(missingManifest.isEmpty()) {
                "Presentation modules with androidDeviceTest missing AndroidManifest.xml declaring ComponentActivity:\n${missingManifest.joinToString("\n") { "  :features:$it:presentation" }}"
            }
        }
    }
})
