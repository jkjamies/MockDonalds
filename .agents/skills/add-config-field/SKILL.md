---
name: add-config-field
description: Add a new field to core:build-config — updates Defaults.properties, every markets/*.properties, the AppBuildConfig facade, and the Phase 1 smoke test. Use when adding compile-time market/env configuration (NOT runtime feature flags — those go to Harness).
---

# Add Config Field

Scaffold a new compile-time config field end-to-end so the next build bakes it in and the architecture-check suite stays green.

**Parameters**:
- `fieldName` (camelCase, e.g. `privacyPolicyUrl`) — required
- `type` — `String` by default. Ask the user if they want `Int`, `Long`, `Boolean`, `Double`, or `Float`. BuildKonfig only supports these primitives; anything richer (lists, nested objects) needs schema work outside this skill.

## First: is this the right home?

Before running the skill, verify the field belongs in `core:build-config` and not Harness. Ask:

- **Can flipping this value be done without a new store release?** If yes → Harness, stop.
- **Does it define the binary's identity, endpoints, legal surface, currency, or locale?** If yes → `core:build-config`, continue.
- **Is it a feature toggle, kill switch, A/B test, or gradual rollout?** If yes → Harness, stop.

If you pattern-match a field name like `*Enabled`, `*Flag`, `*Toggle`, `*Rollout`, or `*Variant`, push back on the user before adding it here.

## Reference Standards

- Full rules: `.agents/standards/build-config.md`
- Module summary: `core/build-config/AGENTS.md`
- Architecture enforcement: `.agents/standards/testing-architecture.md`

## Reference Files

- Facade interface: `core/build-config/src/commonMain/kotlin/com/mockdonalds/app/core/buildconfig/AppBuildConfig.kt`
- Production impl: `core/build-config/src/commonMain/kotlin/com/mockdonalds/app/core/buildconfig/AppBuildConfigImpl.kt` (bound via `@ContributesBinding(AppScope::class)` — no separate providers module)
- Smoke test: `core/build-config/src/commonTest/kotlin/com/mockdonalds/app/core/buildconfig/AppBuildConfigTest.kt`
- Defaults: `core/build-config/Defaults.properties`
- Combo files: `core/build-config/markets/{market}-{env}.properties` (Phase 1: us-dev, us-prod, de-dev, de-prod)
- Konsist coverage rule: `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/core/BuildConfigCoverageTest.kt`

## Steps

### 1. Confirm inputs

Ask the user for the field name if not provided. If `type` wasn't given, ask: *"What type? Default is String. BuildKonfig supports String, Int, Long, Boolean, Double, Float."* Don't proceed until both are known.

Derive the key name for `.properties` files: uppercase snake_case. `privacyPolicyUrl` → `PRIVACY_POLICY_URL`.

### 2. Update `Defaults.properties`

Append:

```
{KEY}={safeDefault}
```

- For `String`: empty is acceptable only if every combo sets it. Prefer a safe fallback (`""` is allowed but discouraged).
- For `Boolean`: `true` or `false`.
- For numerics: a literal.

Ask the user for the default if it isn't obvious.

### 3. Update every `markets/*.properties` file

List every file in `core/build-config/markets/`. For each one, ask the user for the value (or confirm it should fall back to the default). Write `{KEY}={value}` to the file. Do this for **every** combo — a missing override is fine (falls back to default), but every combo file should be considered.

Suggested flow: print the full matrix to the user in one message:

```
us-dev   → PRIVACY_POLICY_URL=?
us-prod  → PRIVACY_POLICY_URL=?
de-dev   → PRIVACY_POLICY_URL=?
de-prod  → PRIVACY_POLICY_URL=?
```

Let them fill in the blanks or say "use default everywhere."

### 4. Expose on `AppBuildConfig` (two files)

**a. Add the abstract property to the interface** in `AppBuildConfig.kt`:

```kotlin
val privacyPolicyUrl: String
```

**b. Override it in the production impl** in `AppBuildConfigImpl.kt`:

```kotlin
override val privacyPolicyUrl: String = BuildConfig.PRIVACY_POLICY_URL
```

Group related fields (URLs together, identity together) rather than appending blindly. Keep the interface and the impl in the same order so diffs stay readable.

**Do not** add a companion object or static accessor. Every consumer receives `AppBuildConfig` via Metro DI (`AppGraph.appBuildConfig` or direct constructor injection).

### 5. Add a test assertion

Add a `Then(...)` block inside the existing `When("reading each field")` scope in `AppBuildConfigTest.kt`. The Konsist `BuildConfigCoverageTest` requires the new property name to appear as `config.<name>` in this file. A minimal assertion:

**String URL:**
```kotlin
Then("privacyPolicyUrl is an https URL") {
    config.privacyPolicyUrl shouldNotBe ""
    config.privacyPolicyUrl shouldMatch Regex("^https://.+")
}
```

**String free-form:**
```kotlin
Then("supportEmail is non-empty") {
    config.supportEmail shouldNotBe ""
}
```

**Int/Long:**
```kotlin
Then("minimumAge is a plausible age") {
    config.minimumAge shouldBeGreaterThanOrEqual 13
}
```

**Boolean:**
```kotlin
Then("someFlag matches the expected bool shape") {
    listOf(true, false) shouldContain config.someFlag
}
```

If none of these fit, write a real assertion on the value's shape. The goal is *coverage existed*, not perfect validation.

### 6. Decide on DI exposure

`AppBuildConfig` as a whole is already injectable via Metro — any presenter, use case, repo, or data source can take it as a constructor parameter and read `appBuildConfig.privacyPolicyUrl` directly. **Default: do nothing here.**

Only add a dedicated `@Provides` fun to `BuildConfigProviders` if the field needs to be injected as its own stand-alone type (e.g. you're introducing a `LegalConfig` sub-interface to make legal-specific consumers easier to test). Ask the user before doing this — it's usually unnecessary ceremony.

### 7. Verify

Run both in parallel:

```bash
./gradlew :core:build-config:testAndroidHostTest
./gradlew :testing:architecture-check:test --tests "com.mockdonalds.app.konsist.core.BuildConfigCoverageTest"
```

Both must pass. If the coverage test fails, the most likely cause is that the test assertion doesn't literally contain `config.<fieldName>` — fix the assertion text, don't weaken the rule.

Then build one non-default combo end-to-end to prove the field bakes in:

```bash
./gradlew :composeApp:assembleDebug -Pmarket=de -Penv=prod
```

### 8. Report

Summarize to the user:

- Field name, type, and key
- Default value in `Defaults.properties`
- Per-combo overrides written
- Whether DI passthrough was added
- Test status (both commands green)

## Out of scope

- **Rich types** (lists, nested data classes): BuildKonfig doesn't support them. If the user asks, explain the limit and offer: (a) a JSON string baked into one field parsed at startup, or (b) escalating to a codegen task modeled after the original `generateActiveMarket` plan in `FUTURE_PLANS.md`.
- **Platform-specific values** (different on Android vs iOS): not supported. If needed, tell the user the value must be the same across platforms, or the field belongs in `actual`-split code elsewhere.
- **Secrets**: never commit secrets to `.properties` files. If the user tries, stop and redirect them to CI-injected env vars.
