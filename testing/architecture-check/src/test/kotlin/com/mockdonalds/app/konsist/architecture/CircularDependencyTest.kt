package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import com.mockdonalds.app.konsist.featurePackageSegment
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Detects circular dependencies between feature modules.
 * If feature A imports from feature B's api, then feature B must not import from feature A's api.
 */
class CircularDependencyTest : BehaviorSpec({

    Given("no circular dependencies between features") {
        Then("feature modules should not have circular api imports") {
            val featureFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }

            // Build a dependency graph: feature -> set of features it imports from
            val dependencyGraph = mutableMapOf<String, MutableSet<String>>()

            featureFiles.forEach { file ->
                // Must be the package segment, not the directory name. `otherFeature` below is
                // parsed out of an import with `\.features\.(\w+)\.`, and `\w` cannot match a
                // hyphen — so for `debug-menu` the two sides could never agree: its own imports
                // registered as cross-feature ("debug-menu" != "debugmenu"), while every other
                // feature recorded it as "debugmenu", leaving the reciprocal lookup below unable
                // to pair them. A genuine cycle involving the repo's one hyphenated feature was
                // undetectable.
                val featureName = featurePackageSegment(file.path)
                file.imports.forEach { import ->
                    val match = Regex("\\.features\\.(\\w+)\\.").find(import.name)
                    if (match != null) {
                        val otherFeature = match.groupValues[1]
                        if (otherFeature != featureName) {
                            dependencyGraph.getOrPut(featureName) { mutableSetOf() }.add(otherFeature)
                        }
                    }
                }
            }

            // Check for direct circular dependencies (A -> B and B -> A)
            val cycles = mutableListOf<String>()
            dependencyGraph.forEach { (feature, dependencies) ->
                dependencies.forEach { dep ->
                    if (dependencyGraph[dep]?.contains(feature) == true) {
                        val pair = listOf(feature, dep).sorted()
                        val cycle = "${pair[0]} <-> ${pair[1]}"
                        if (cycle !in cycles) cycles.add(cycle)
                    }
                }
            }

            assert(cycles.isEmpty()) {
                "Circular dependencies detected between features:\n${cycles.joinToString("\n") { "  $it" }}"
            }
        }
    }
})
