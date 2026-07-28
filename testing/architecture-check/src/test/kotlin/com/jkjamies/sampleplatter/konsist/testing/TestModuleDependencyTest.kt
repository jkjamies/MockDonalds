package com.jkjamies.sampleplatter.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Enforces the invariant that `test/` sibling modules depend ONLY on `api/` modules —
 * never on `impl/`. Test graphs must stay free of impl-only dependencies (vendor SDKs,
 * platform-specific code, BuildKonfig-generated classes) so consumer tests can swap in
 * fakes without pulling production wiring.
 *
 * Also checks that test module source files do not import from sibling impl packages.
 *
 * See .agents/standards/dependency-injection.md → "Test modules depend only on `api`".
 */
class TestModuleDependencyTest : BehaviorSpec({

    val projectRoot = Konsist.scopeFromProject()
        .files
        .first()
        .path
        .let { path ->
            var dir: File? = File(path).parentFile
            while (dir != null && !dir.resolve("settings.gradle.kts").exists()) {
                dir = dir.parentFile
            }
            dir ?: error("Could not find project root (settings.gradle.kts) from $path")
        }

    // All test/ sibling modules under features/ and core/. Excludes infrastructure like
    // core:test-fixtures (itself a test-fixture, not a test sibling).
    fun testModuleBuildFiles(): List<File> {
        val roots = listOf(projectRoot.resolve("features"), projectRoot.resolve("core"))
        return roots
            .filter { it.exists() }
            .flatMap { root ->
                root.listFiles()
                    ?.filter { it.isDirectory }
                    ?.mapNotNull { module ->
                        module.resolve("test/build.gradle.kts").takeIf { it.exists() }
                    }
                    .orEmpty()
            }
    }

    Given("test module build.gradle.kts dependencies") {
        Then("no test module should declare a project dependency on an :impl module") {
            val violators = testModuleBuildFiles().flatMap { buildFile ->
                // Match project(":some:module:impl") or project(":some:module:impl:...")
                val regex = Regex("""project\("(:[^"]*:impl(?::[^"]*)?)"\)""")
                regex.findAll(buildFile.readText())
                    .map { match ->
                        val relative = buildFile.relativeTo(projectRoot).path
                        "  $relative → ${match.groupValues[1]}"
                    }
            }

            assert(violators.isEmpty()) {
                "test/ modules must depend only on :api modules — never on :impl. " +
                    "If the test graph needs a DI slot declared by impl (e.g. a @Multibinds " +
                    "contract), declare a parallel copy in the test module instead:\n" +
                    violators.joinToString("\n")
            }
        }
    }

    Given("test module source imports") {
        Then("files in test module commonMain should not import from sibling impl packages") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..test/src/commonMain..") }
                .flatMap { file ->
                    file.imports
                        .filter { imp ->
                            val name = imp.name
                            // Catch core .impl. and features .impl. (but allow impl.presentation
                            // re-exports if ever needed — test modules don't do that today, and
                            // we want to flag everything impl).
                            name.contains(".core.") && name.contains(".impl.") ||
                                name.contains(".features.") && name.contains(".impl.")
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "test/ modules must not import from sibling impl packages — depend on api only:\n" +
                    violators.joinToString("\n")
            }
        }
    }
})
