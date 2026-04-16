import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.net.URI
import java.util.Properties

plugins {
    id("mockdonalds.kmp.domain")
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.buildconfig"
    }
}

val market: String = providers.gradleProperty("market").getOrElse("us")
val env: String = providers.gradleProperty("env").getOrElse("dev")

logger.lifecycle("core:build-config → market=$market env=$env")

fun loadProps(path: String): Map<String, String> {
    val f = file(path)
    require(f.exists()) { "Missing build-config file: $path" }
    val props = Properties().apply { f.inputStream().use { load(it) } }
    return props.entries.associate { (k, v) -> k.toString() to v.toString() }
}

val defaults = loadProps("Defaults.properties")
val comboPath = "markets/$market-$env.properties"
require(file(comboPath).exists()) {
    "Unknown market/env combo: $market-$env (expected $comboPath)"
}
val combo = loadProps(comboPath)

val merged: Map<String, String> = defaults + combo

buildkonfig {
    packageName = "com.mockdonalds.app.core.buildconfig"
    objectName = "BuildConfig"
    defaultConfigs {
        merged.forEach { (key, value) ->
            buildConfigField(STRING, key, value)
        }
    }
}

// ─── validateAllMarkets ──────────────────────────────────────────────────────
// Parse every markets/*.properties against Defaults.properties and enforce
// the invariants documented in .agents/standards/build-config.md → "Validation
// rules". Aggregates ALL violations across ALL files in one pass; never
// fail-fast. Runs as a pre-flight gate in the `verify full` and `verify all` scopes.
val defaultsFile = layout.projectDirectory.file("Defaults.properties")
val marketsDir = layout.projectDirectory.dir("markets")

tasks.register("validateAllMarkets") {
    group = "verification"
    description = "Validate every markets/*.properties against Defaults.properties schema and format rules."

    // Deliberately no `outputs.upToDateWhen { true }` — that would skip execution when
    // input files are unchanged but the validation logic in this file has changed,
    // letting new rules silently miss existing violations. The task runs in ~1s warm;
    // always executing is cheaper than being subtly wrong.
    inputs.file(defaultsFile)
    inputs.dir(marketsDir)

    val defaultsAsFile = defaultsFile.asFile
    val marketsAsDir = marketsDir.asFile

    doLast {
        val errors = mutableListOf<String>()
        val knownEnvs = setOf("dev", "stg", "prod")
        val filenameRe = Regex("""^([a-z]{2})-([a-z]+)\.properties$""")
        val localeRe = Regex("""^[a-z]{2}-[A-Z]{2}$""")
        val currencyRe = Regex("""^[A-Z]{3}$""")
        val marketRe = Regex("""^[a-z]{2}$""")

        fun parseProperties(file: File): Pair<Map<String, String>, List<String>> {
            val seen = linkedMapOf<String, String>()
            val dupes = mutableListOf<String>()
            file.readLines().forEachIndexed { idx, raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) return@forEachIndexed
                val eq = line.indexOf('=')
                if (eq < 0) {
                    dupes += "${file.name}:${idx + 1}: malformed line '$raw' (expected KEY=value)"
                    return@forEachIndexed
                }
                val k = line.substring(0, eq).trim()
                val v = line.substring(eq + 1).trim()
                if (k in seen) dupes += "${file.name}:${idx + 1}: duplicate key '$k'"
                seen[k] = v
            }
            return seen to dupes
        }

        fun validateUrl(field: String, value: String): String? {
            if (value.isEmpty()) return "$field: empty"
            val parsed = runCatching { URI(value) }.getOrNull()
                ?: return "$field: not a valid URI (got '$value')"
            if (parsed.scheme != "https") return "$field: must use https:// (got '$value')"
            if (parsed.host.isNullOrBlank()) return "$field: no host (got '$value')"
            if (value.endsWith("/")) return "$field: trailing slash not allowed (got '$value')"
            if ("$" in value) return "$field: interpolation tokens not allowed (got '$value')"
            return null
        }

        if (!defaultsAsFile.exists()) {
            throw GradleException("validateAllMarkets: ${defaultsAsFile.name} missing")
        }
        val (defaults, defaultsDupes) = parseProperties(defaultsAsFile)
        errors += defaultsDupes
        if (defaults.isEmpty()) errors += "${defaultsAsFile.name}: empty (must declare schema)"

        if (!marketsAsDir.exists()) {
            throw GradleException("validateAllMarkets: ${marketsAsDir.name}/ missing")
        }

        data class Combo(val file: File, val market: String, val env: String)
        val combos = mutableListOf<Combo>()
        val marketEnvs = linkedMapOf<String, MutableSet<String>>()

        marketsAsDir.listFiles().orEmpty().sortedBy { it.name }.forEach { entry ->
            if (!entry.isFile) {
                errors += "markets/${entry.name}: not a regular file"
                return@forEach
            }
            val m = filenameRe.matchEntire(entry.name)
            if (m == null) {
                errors += "markets/${entry.name}: filename must match {market}-{env}.properties (lowercase)"
                return@forEach
            }
            val (mk, ev) = m.destructured
            combos += Combo(entry, mk, ev)
            marketEnvs.getOrPut(mk) { mutableSetOf() }.add(ev)
        }

        val allEnvsUsed = marketEnvs.values.flatten().toSortedSet()
        marketEnvs.toSortedMap().forEach { (mk, envs) ->
            (allEnvsUsed - envs).sorted().forEach { missing ->
                errors += "markets/$mk-$missing.properties: missing — '$mk' has envs ${envs.sorted()} but other markets define '$missing'"
            }
        }

        combos.forEach { (file, expectedMarket, expectedEnv) ->
            val (combo, dupes) = parseProperties(file)
            errors += dupes

            combo.keys.forEach { key ->
                if (key !in defaults) {
                    val suggestion = defaults.keys.firstOrNull { it.equals(key, ignoreCase = true) }
                        ?.let { " (did you mean '$it'?)" } ?: ""
                    errors += "${file.name}: unknown key '$key'$suggestion"
                }
            }

            val merged = defaults + combo
            defaults.keys.forEach { key ->
                if (merged[key].orEmpty().isBlank()) {
                    errors += "${file.name}: missing required value for '$key'"
                }
            }

            merged["MARKET"]?.takeIf { it.isNotEmpty() }?.let { marketVal ->
                if (marketVal != expectedMarket) {
                    errors += "${file.name}: MARKET='$marketVal' does not match filename market '$expectedMarket'"
                }
                if (!marketRe.matches(marketVal)) {
                    errors += "${file.name}: MARKET='$marketVal' must be 2 lowercase letters"
                }
            }
            merged["ENV"]?.takeIf { it.isNotEmpty() }?.let { envVal ->
                if (envVal != expectedEnv) {
                    errors += "${file.name}: ENV='$envVal' does not match filename env '$expectedEnv'"
                }
                if (envVal !in knownEnvs) {
                    errors += "${file.name}: ENV='$envVal' not in known envs ${knownEnvs.sorted()}"
                }
            }
            merged["LOCALE"]?.takeIf { it.isNotEmpty() }?.let { locale ->
                if (!localeRe.matches(locale)) {
                    errors += "${file.name}: LOCALE='$locale' must be BCP 47 form xx-XX"
                }
            }
            merged["CURRENCY"]?.takeIf { it.isNotEmpty() }?.let { currency ->
                if (!currencyRe.matches(currency)) {
                    errors += "${file.name}: CURRENCY='$currency' must be 3 uppercase letters (ISO 4217)"
                }
            }
            merged.forEach { (key, value) ->
                if (key.endsWith("_URL") && value.isNotEmpty()) {
                    validateUrl("${file.name}: $key", value)?.let { errors += it }
                }
            }
            merged["APP_NAME"]?.takeIf { it.isNotEmpty() }?.let { appName ->
                if (appName != appName.trim()) {
                    errors += "${file.name}: APP_NAME has leading/trailing whitespace"
                }
            }
        }

        if (errors.isNotEmpty()) {
            val message = buildString {
                appendLine("validateAllMarkets: ${errors.size} violation(s):")
                errors.forEach { appendLine("  $it") }
            }
            throw GradleException(message.trimEnd())
        }

        val summary = combos.joinToString(", ") { "${it.market}-${it.env}" }
        logger.lifecycle("validateAllMarkets: OK (${combos.size} combos: $summary)")
    }
}
