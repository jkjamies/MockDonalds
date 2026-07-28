package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Keeps the agent entry-point docs in sync with what is actually on disk.
 *
 * `AgentDocumentationTest` checks that agentic files *exist*. This checks that they are
 * *true* — that every feature, core module, skill, and standard on disk is named in the
 * docs an agent reads first.
 *
 * This matters more here than in a normal repo. Agents route off `AGENTS.md`: an
 * unlisted skill is an uninvokable one, and an unlisted feature is one a scaffolding skill
 * will not model its output on. Drift is silent, one-directional (additions land, docs
 * lag), and cumulative — a review found ten features documented as seven, thirty-two
 * skills documented as twenty-nine, and a `profile` skill listed that had been renamed to
 * `benchmark`.
 *
 * The check is deliberately one-directional and name-based: it asserts every real name
 * appears somewhere in the doc, not that the doc contains nothing else. Prose freedom is
 * preserved; only omissions fail.
 */
class AgentDocumentationDriftTest : BehaviorSpec({

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

    fun directoryNames(relativePath: String): List<String> =
        projectRoot.resolve(relativePath)
            .listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { it.name }
            .sorted()

    fun assertAllNamed(names: List<String>, docPath: String, label: String) {
        val doc = projectRoot.resolve(docPath)
        assert(doc.exists()) { "$docPath is missing" }
        val text = doc.readText()
        val missing = names.filterNot { text.contains(it) }

        assert(missing.isEmpty()) {
            "$docPath does not mention every $label on disk. Add the missing entries — " +
                "an agent routes off this file, so anything unlisted is effectively invisible:\n" +
                missing.joinToString("\n") { "  $it" }
        }
    }

    Given("feature inventory") {
        val features = directoryNames("features")

        Then("every feature should be named in the root AGENTS.md") {
            assertAllNamed(features, "AGENTS.md", "feature")
        }

        Then("every feature should be named in the README") {
            assertAllNamed(features, "README.md", "feature")
        }
    }

    Given("core module inventory") {
        val coreModules = directoryNames("core")

        Then("every core module should be named in the root AGENTS.md") {
            assertAllNamed(coreModules, "AGENTS.md", "core module")
        }

        Then("every core module should be named in the README") {
            assertAllNamed(coreModules, "README.md", "core module")
        }
    }

    Given("skill inventory") {
        val skills = directoryNames(".agents/skills")

        Then("every skill should be listed in the root AGENTS.md") {
            assertAllNamed(skills, "AGENTS.md", "skill")
        }

        Then("every skill should be listed in .agents/AGENTS.md") {
            assertAllNamed(skills, ".agents/AGENTS.md", "skill")
        }

        Then("no skill named in the docs should be missing from disk") {
            // Catches the reverse drift: a renamed or deleted skill left behind in the tables,
            // which sends an agent looking for a SKILL.md that is not there.
            val skillsDir = projectRoot.resolve(".agents/skills")
            val documented = listOf("AGENTS.md", ".agents/AGENTS.md")
                .flatMap { doc ->
                    Regex("""`([a-z][a-z0-9]*(?:-[a-z0-9]+)+)`""")
                        .findAll(projectRoot.resolve(doc).readText())
                        .map { it.groupValues[1] }
                }
                .distinct()
                // Only names that look like skill invocations — a hyphenated token that is
                // neither a real directory elsewhere in the repo nor a known non-skill term.
                .filter { it.startsWith("add-") || it.startsWith("run-") || it.endsWith("-spec") }
                .filterNot { it == "new-spec" || it == "change-spec" || it == "migrate-spec" || it == "remove-spec" }

            val phantom = documented.filterNot { skillsDir.resolve(it).isDirectory }

            assert(phantom.isEmpty()) {
                "Skills named in the docs that do not exist under .agents/skills/:\n" +
                    phantom.joinToString("\n") { "  $it" }
            }
        }
    }

    Given("architecture rule count") {
        Then("the count quoted in the root AGENTS.md should match the suite") {
            val actual = projectRoot.resolve("testing/architecture-check/src/test")
                .walkTopDown()
                .count { it.isFile && it.name.endsWith("Test.kt") }

            val quoted = Regex("""(\d+) test classes in `testing/architecture-check/`""")
                .find(projectRoot.resolve("AGENTS.md").readText())
                ?.groupValues
                ?.get(1)
                ?.toInt()

            assert(quoted == actual) {
                "AGENTS.md says $quoted Konsist test classes; the suite has $actual. " +
                    "A stale count is how a reader learns the docs are not maintained — " +
                    "update the Tech Stack table."
            }
        }
    }

    Given("standards inventory") {
        val standards = projectRoot.resolve(".agents/standards")
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "md" }
            .map { it.name }
            .sorted()

        Then("every standard should be listed in the root AGENTS.md standards table") {
            assertAllNamed(standards, "AGENTS.md", "standards document")
        }

        Then("every standard should be enforced by AgentDocumentationTest") {
            // The allowlist in AgentDocumentationTest is hand-maintained; without this, a new
            // standard can be added and silently never checked for existence.
            val enforcementSource = projectRoot
                .resolve(
                    "testing/architecture-check/src/test/kotlin/com/jkjamies/sampleplatter/konsist/" +
                        "core/AgentDocumentationTest.kt",
                )
                .readText()

            val missing = standards.filterNot { enforcementSource.contains("\"$it\"") }

            assert(missing.isEmpty()) {
                "Standards not present in the AgentDocumentationTest allowlist:\n" +
                    missing.joinToString("\n") { "  $it" }
            }
        }
    }
})
