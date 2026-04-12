---
name: validate-all-markets
description: Parse every core/build-config/markets/*.properties against Defaults.properties and enforce build-config invariants (required keys, no unknown keys, format rules for market/env/locale/currency/URLs). Gradle task on :core:build-config — sub-second warm, configuration-cache compatible. Run before any market-scoped build and as part of `verify`.
---

# Validate All Markets

Prove every market+env combo file is internally consistent and matches the schema declared by `Defaults.properties`. Catches drift before it reaches a build.

**The authoritative rule list lives in `.agents/standards/build-config.md` → "Validation rules".** This skill is the executable form of those rules. If a rule changes there, update the `validateAllMarkets` task in `core/build-config/build.gradle.kts` in the same change.

## When to run

- Before any market-scoped build (`./gradlew :composeApp:assembleDebug -Pmarket=de -Penv=prod`)
- As part of `verify` and `verify-ci` (already wired in)
- After editing anything under `core/build-config/markets/` or `core/build-config/Defaults.properties`
- Before opening a PR that touches build-config
- Before adding a new market or new env

The task executes on every invocation by design — it deliberately does NOT use `upToDateWhen { true }`, because the validation logic lives in `build.gradle.kts` and isn't tracked as a Gradle input. Skipping when file inputs are unchanged would mean a newly added rule silently misses pre-existing violations. Warm with configuration cache it runs in well under a second; the first cold invocation after a config-cache invalidation pays normal Gradle startup.

## Steps

### 1. Run the task

```bash
./gradlew :core:build-config:validateAllMarkets
```

Exit code 0 = clean (`validateAllMarkets: OK (N combos: ...)`). Non-zero = at least one violation; the task aggregates EVERY violation across EVERY file in one pass and prints them in the `GradleException` message. Never fix one and re-run hoping; fix the whole list, then re-run once.

### 2. Interpret failures

The output format is `{file}: {what went wrong}`. The most common categories:

| Message shape | Cause | Fix |
|---|---|---|
| `missing required value for 'X'` | Combo doesn't override and `Defaults.properties` is blank | Set `X=...` in the combo file |
| `unknown key 'X' (did you mean 'Y'?)` | Typo introduced in a combo file | Rename to match the schema in `Defaults.properties` |
| `MARKET='us' does not match filename market 'de'` | Copy-paste error after duplicating a combo file | Set `MARKET=` to match the filename segment |
| `ENV='staging' not in known envs` | Used a long-form env name | Use `dev` / `stg` / `prod` |
| `LOCALE='de' must be BCP 47 form xx-XX` | Bare language tag | Use `de-DE` / `en-US` form |
| `CURRENCY='eur' must be 3 uppercase letters` | Lowercase or symbol | Use ISO 4217 (`EUR`, `USD`) |
| `BASE_URL: must use https://` | http/missing scheme | Use https + no trailing slash + no `$market` tokens |
| `missing — 'us' has envs [dev, prod] but other markets define 'stg'` | Asymmetric markets | Add the missing combo file |
| `unknown key 'X'` (no suggestion) | Combo introduces a key not in `Defaults.properties` | Either add it to defaults (if it's a new field for ALL markets) or remove it (if it's dead) |

### 3. Add a new validation rule

If you need to enforce a new invariant:

1. Document it in `.agents/standards/build-config.md` → "Validation rules" — that's the source of truth.
2. Implement it in the `validateAllMarkets` task in `core/build-config/build.gradle.kts` (search for `// ─── validateAllMarkets`).
3. Add a quick negative test by temporarily breaking a combo file, running the task, confirming the new violation fires, then restoring the file.
4. If the rule needs typed Kotlin reflection (e.g. checking against the `AppBuildConfig` interface), it belongs in Konsist instead — add it to `BuildConfigCoverageTest.kt` or a sibling rule, not here.

The split between this task and Konsist is deliberate: `validateAllMarkets` runs against raw `.properties` files and gates every build; Konsist runs against compiled metadata and owns rules that need a typed view of the code.

## Related

- `.agents/standards/build-config.md` — full schema, facade rules, validation rules
- `.agents/skills/add-config-field/` — add a new compile-time field end-to-end
- `core/build-config/build.gradle.kts` — task implementation
- `core/build-config/AGENTS.md` — module-level summary
- `testing/architecture-check/.../BuildConfigCoverageTest.kt` — the typed-Kotlin counterpart
