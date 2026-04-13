package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Validates that agentic automation files are present and correctly configured.
 *
 * Every feature, core module, and key project area must have an AGENTS.md file
 * so AI agents discover project conventions via JIT context loading.
 * The root AGENTS.md and skills directory must also exist.
 */
class AgentDocumentationTest : BehaviorSpec({

    val projectRoot = Konsist.scopeFromProject()
        .files
        .first()
        .path
        .let { path ->
            var dir = File(path).parentFile
            while (dir != null && !dir.resolve("settings.gradle.kts").exists()) {
                dir = dir.parentFile
            }
            dir ?: error("Could not find project root (settings.gradle.kts) from $path")
        }

    Given("root agentic infrastructure") {
        Then("project root should have an AGENTS.md file") {
            val agentsMd = projectRoot.resolve("AGENTS.md")
            assert(agentsMd.exists()) {
                "Project root must have an AGENTS.md file for agent convention discovery"
            }
        }

        Then("project should have a .agents/skills/ directory") {
            val skillsDir = projectRoot.resolve(".agents/skills")
            assert(skillsDir.exists() && skillsDir.isDirectory) {
                "Project must have a .agents/skills/ directory containing automation skills"
            }
        }

        Then("every skill directory should have a SKILL.md file") {
            val skillsDir = projectRoot.resolve(".agents/skills")
            if (skillsDir.exists()) {
                val violators = skillsDir.listFiles()
                    ?.filter { it.isDirectory }
                    ?.filter { !it.resolve("SKILL.md").exists() }
                    ?.map { it.name }
                    ?: emptyList()

                assert(violators.isEmpty()) {
                    "Skill directories missing SKILL.md:\n${violators.joinToString("\n") { "  .agents/skills/$it/" }}"
                }
            }
        }
    }

    Given("standards reference library") {
        Then("project should have a .agents/standards/ directory with all standard files") {
            val standardsDir = projectRoot.resolve(".agents/standards")
            assert(standardsDir.exists() && standardsDir.isDirectory) {
                "Project must have a .agents/standards/ directory containing reference standards"
            }
            val expectedFiles = listOf(
                "architecture.md",
                "naming-conventions.md",
                "dependency-injection.md",
                "testing.md",
                "testing-unit.md",
                "testing-ui-component.md",
                "testing-navint.md",
                "testing-e2e.md",
                "testing-architecture.md",
                "centerpost.md",
                "forbidden-patterns.md",
                "verification.md",
                "ios-interop.md",
                "ways-of-working.md",
                "code-style.md",
                "design-system.md",
                "convention-plugins.md",
                "feature-scaffolding.md",
                "build-config.md",
            )
            val missing = expectedFiles.filter { !standardsDir.resolve(it).exists() }
            assert(missing.isEmpty()) {
                "Missing standards files:\n${missing.joinToString("\n") { "  .agents/standards/$it" }}"
            }
        }
    }

    Given("Gemini CLI configuration") {
        Then("project should have a .gemini/settings.json with AGENTS.md context") {
            val settingsFile = projectRoot.resolve(".gemini/settings.json")
            assert(settingsFile.exists()) {
                "Project must have .gemini/settings.json to configure Gemini CLI to read AGENTS.md files"
            }
            if (settingsFile.exists()) {
                val content = settingsFile.readText()
                assert(content.contains("AGENTS.md")) {
                    ".gemini/settings.json must configure context.fileName to include \"AGENTS.md\""
                }
            }
        }
    }

    Given("feature module agent documentation") {
        Then("every feature should have an AGENTS.md file") {
            val featuresDir = projectRoot.resolve("features")
            val violators = featuresDir.listFiles()
                ?.filter { it.isDirectory }
                ?.filter { !it.resolve("AGENTS.md").exists() }
                ?.map { it.name }
                ?: emptyList()

            assert(violators.isEmpty()) {
                "Feature modules missing AGENTS.md:\n${violators.joinToString("\n") { "  features/$it/" }}"
            }
        }
    }

    Given("core module agent documentation") {
        Then("every core module should have an AGENTS.md file") {
            val coreDir = projectRoot.resolve("core")
            val violators = coreDir.listFiles()
                ?.filter { it.isDirectory }
                ?.filter { !it.resolve("AGENTS.md").exists() }
                ?.map { it.name }
                ?: emptyList()

            assert(violators.isEmpty()) {
                "Core modules missing AGENTS.md:\n${violators.joinToString("\n") { "  core/$it/" }}"
            }
        }
    }
})
