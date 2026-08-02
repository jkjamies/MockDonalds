package com.jkjamies.sampleplatter.konsist

import java.io.File

/**
 * Path predicates shared by the architecture rules.
 *
 * These are deliberately plain `String` functions rather than extensions on Konsist
 * declaration types: every rule file already has a `path` in hand, and keeping the helpers
 * free of Konsist types means they stay valid across Konsist API changes.
 */

/**
 * Konsist builds `path` from the host filesystem, so it carries the OS separator — backslashes
 * on Windows.
 *
 * Every predicate here, and every rule that parses a path directly, matches on `/`. An
 * unnormalized Windows path therefore matches nothing: the filter empties, the rule inspects
 * zero declarations and reports PASSED. That is the same silent-vacuum failure as a
 * `commonMain`-only scope, except it only appears off CI — both CI runners are Unix, so the
 * suite stays green while doing nothing on a Windows contributor's machine.
 *
 * Normalize before matching, never after.
 */
fun normalizedPath(path: String): String = path.replace('\\', '/')

/**
 * True for every production source set — `commonMain`, `androidMain`, `iosMain`.
 *
 * Scoping an architecture rule to `commonMain` alone is the most common way for a rule to
 * look enforced while checking almost nothing. In this project every `*Ui.kt` lives in
 * `androidMain` and the Molecule/Circuit bridge lives in `iosMain`, so a `commonMain`-only
 * filter leaves the single largest body of code in the repo unchecked — a presenter could
 * import from `impl/data`, or a UI file could use `GlobalScope`, and every rule would pass.
 */
fun isProductionSourcePath(path: String): Boolean = normalizedPath(path).let {
    it.contains("/commonMain/") || it.contains("/androidMain/") || it.contains("/iosMain/")
}

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
    normalizedPath(path).substringAfter("features/").substringBefore("/").replace("-", "")

/** True for files inside any `core/{module}/impl/` source set. */
fun isCoreImplPath(path: String): Boolean =
    Regex("(^|/)core/[^/]+/impl/").containsMatchIn(normalizedPath(path))

/**
 * The project root: the nearest ancestor of [path] holding both `settings.gradle.kts` and
 * `features/`.
 *
 * Rules that reach the filesystem (listing `features/` to find modules Konsist cannot see, such
 * as ones with no Kotlin sources) need a root to anchor `File(...)` against. Deriving one by
 * cutting the path at `/features/` looks equivalent and is not. Callers pass
 * `scopeFromProject().files.first().path` — whatever file the scan happens to enumerate first,
 * routinely under `androidApp/` or `build-logic/`. Neither contains the delimiter, and
 * `substringBefore` returns the whole string when the delimiter is absent, so the "root" becomes
 * a full file path, `File("$root/features").listFiles()` returns null, the module list is empty
 * and the rule passes having checked nothing.
 *
 * Both markers are required because `build-logic` is an included build with its own
 * `settings.gradle.kts`: matching on that file alone stops one directory too early and lands on a
 * root with no `features/`, which is the same vacuum by another route.
 *
 * Failing loudly is the point — a root this function cannot resolve must not degrade into an
 * empty module list. [path] is passed to `File` unnormalized on purpose: it resolves the host
 * separator itself, so this is correct on Windows without string surgery.
 */
fun projectRootFrom(path: String): File {
    var dir = File(path).parentFile
    while (dir != null &&
        !(dir.resolve("settings.gradle.kts").isFile && dir.resolve("features").isDirectory)
    ) {
        dir = dir.parentFile
    }
    return dir ?: error(
        "Could not find the project root (a directory holding settings.gradle.kts and features/) from '$path'",
    )
}
