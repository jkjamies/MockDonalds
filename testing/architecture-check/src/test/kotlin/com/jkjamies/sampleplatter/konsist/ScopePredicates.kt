package com.jkjamies.sampleplatter.konsist

/**
 * Path predicates shared by the architecture rules.
 *
 * These are deliberately plain `String` functions rather than extensions on Konsist
 * declaration types: every rule file already has a `path` in hand, and keeping the helpers
 * free of Konsist types means they stay valid across Konsist API changes.
 */

/**
 * True for every production source set — `commonMain`, `androidMain`, `iosMain`.
 *
 * Scoping an architecture rule to `commonMain` alone is the most common way for a rule to
 * look enforced while checking almost nothing. In this project every `*Ui.kt` lives in
 * `androidMain` and the Molecule/Circuit bridge lives in `iosMain`, so a `commonMain`-only
 * filter leaves the single largest body of code in the repo unchecked — a presenter could
 * import from `impl/data`, or a UI file could use `GlobalScope`, and every rule would pass.
 */
fun isProductionSourcePath(path: String): Boolean =
    path.contains("/commonMain/") || path.contains("/androidMain/") || path.contains("/iosMain/")

/**
 * The feature's Kotlin package segment, derived from its directory name.
 *
 * Directory names may contain hyphens (`debug-menu`); Kotlin package segments may not, so
 * the convention strips them (`debugmenu`). Comparing a raw directory name against a
 * package segment silently disables any rule that does so for hyphenated features: the
 * names never match, so same-feature imports read as cross-feature violations and
 * sibling-layer imports read as clean.
 */
fun featurePackageSegment(path: String): String =
    path.substringAfter("features/").substringBefore("/").replace("-", "")

/** True for files inside any `core/{module}/impl/` source set. */
fun isCoreImplPath(path: String): Boolean =
    Regex("(^|/)core/[^/]+/impl/").containsMatchIn(path)
