---
name: find-dead-code
description: Surface unused Kotlin declarations (imports, private members, parameters) across ALL source sets via full Detekt, plus targeted greps for dead TestTags, Screen objects with no navigation, and Fake classes with no test references. Reports findings — does not auto-delete. Run periodically to stop dead code accumulating.
---

# Find Dead Code

Surface code that exists but isn't used — so you can delete it. This skill reports; it never deletes. Dead-code removal is a judgment call (the declaration may be reserved for an upcoming feature, required by a framework contract, or exercised only by tests you haven't discovered yet), so the human always decides what goes.

Target runtime: ~30–60s cold depending on repo size.

## What this covers

1. **Detekt `Unused*` rules across ALL source sets** — `UnusedImports`, `UnusedPrivateMember`, `UnusedParameter`. Already configured in `config/detekt/detekt.yml` but only surfaced via `detektMetadataCommonMain` in the normal `verify` loop. This skill runs the full `detekt` task graph, which covers `commonMain`, `androidMain`, `iosMain`, and every test source set.
2. **Dead `*TestTags` constants** — `object *TestTags { const val ... }` entries that are never referenced in tests or UI composables. Common after a test is deleted or a UI element gets renamed without updating the tag.
3. **Dead `*Screen` objects** — `data object *Screen : ... Screen` with zero references in `navigator.goTo(` calls AND zero `@CircuitInject(*Screen::class` registrations. Catches screens left behind after a nav refactor.
4. **Dead `Fake*` classes** — test doubles under `features/*/test/src/commonMain/` whose class name never appears in any `*Test.kt` file. Happens when you swap one fake for another but leave the old one behind.

## What this does NOT cover

- **Swift dead code** — needs [Periphery](https://github.com/peripheryapp/periphery) (`brew install periphery`). Not currently integrated; add it if Swift-side dead code becomes a real problem. Most shared logic lives in KMP Kotlin anyway.
- **Dead public Kotlin declarations across modules** — requires cross-module reference analysis. Detekt's `UnusedPrivateMember` only covers `private` scope. For public declarations, IntelliJ's "Unused declaration" inspection is still the gold standard; this skill doesn't replicate it.
- **Dead Metro bindings** — a `@ContributesBinding(AppScope::class)` class whose interface is never injected anywhere. Konsist could express this but doesn't today. If you see it slipping, add a `DependencyInjectionTest` rule.
- **Dead resources** — string resources, drawables, XML. Compose doesn't use XML layouts so this is small surface; Android Lint covers it if you need it.

## Steps

### 1. Full Detekt sweep

```bash
./gradlew detekt
```

This runs every `detekt*` task in every module, covering commonMain + every platform source set + every test source set. Review the output for `UnusedImports` / `UnusedPrivateMember` / `UnusedParameter` findings.

Most unused-import findings auto-correct (`autoCorrect = true`). Private-member findings require manual review: either the member is actually dead (delete it) or it's reserved for use (suppress with `@Suppress("UnusedPrivateMember")` and a comment explaining why).

### 2. Dead TestTags

Find every `*TestTags` constant name, then grep for usage outside its declaring file:

```bash
# List all tag constants defined
grep -rhn 'const val [A-Z_]*' --include='*TestTags.kt' \
  | sed -E 's/.*const val ([A-Z_]+).*/\1/' \
  | sort -u > /tmp/tags-defined.txt

# For each, check if it's referenced anywhere except its declaration
while read -r tag; do
    count=$(grep -r "$tag" --include='*.kt' --include='*.swift' -l \
        | grep -v 'TestTags.kt$' | wc -l)
    if [ "$count" -eq 0 ]; then
        echo "dead: $tag"
    fi
done < /tmp/tags-defined.txt
```

Report any `dead: *` lines. A dead tag means either the tag is unused (delete it) or the test that should reference it was never written (add the test — see `add-ui-tests`).

### 3. Dead Screens

List every Screen object, then check each for navigation and CircuitInject references:

```bash
grep -rh 'data object [A-Za-z_]*Screen[[:space:]]*:' --include='*Screen.kt' \
  | sed -E 's/.*data object ([A-Za-z_]+Screen).*/\1/' \
  | sort -u > /tmp/screens-defined.txt

while read -r screen; do
    nav_refs=$(grep -r "goTo($screen" --include='*.kt' -l | wc -l)
    circuit_refs=$(grep -r "@CircuitInject($screen::class" --include='*.kt' -l | wc -l)
    deeplink_refs=$(grep -r "$screen" --include='*.kt' -l \
        | xargs grep -l 'DeepLink\|deepLink' 2>/dev/null | wc -l)
    if [ "$nav_refs" -eq 0 ] && [ "$circuit_refs" -eq 0 ] && [ "$deeplink_refs" -eq 0 ]; then
        echo "dead: $screen"
    fi
done < /tmp/screens-defined.txt
```

A dead screen means either the screen was orphaned by a nav refactor (delete it + its presenter/UI/TestTags) or it's reachable only by deep link and the grep missed it — verify before deleting. Check `composeApp/` for deep-link wiring.

### 4. Dead Fakes

```bash
find features -path '*/test/src/commonMain/*' -name 'Fake*.kt' \
  | while read -r file; do
      name=$(basename "$file" .kt)
      refs=$(grep -r "$name" --include='*Test.kt' -l | wc -l)
      if [ "$refs" -eq 0 ]; then
          echo "dead: $file"
      fi
  done
```

A dead fake means either the fake was replaced (delete it) or a planned test was never written (add the test).

### 5. Report

Collect findings into a single summary:

```
find-dead-code report

Detekt unused declarations: N findings (see output above)
Dead TestTags: [list or "none"]
Dead Screens: [list or "none"]
Dead Fakes: [list or "none"]
```

**Do NOT delete anything automatically.** Present the list to the user and let them decide. Some findings are false positives (reserved for upcoming work, framework contracts, test data). The value is in the surface area, not the auto-fix.

## Related

- `.agents/standards/code-style.md` — Detekt rule configuration
- `.agents/standards/forbidden-patterns.md` — architectural dead-code patterns (e.g. leftover `ViewModel` after a Circuit migration)
- `.agents/skills/lint-branch/` — fast pre-commit lint check (commonMain only)
- `.agents/skills/verify/` — full verification pipeline
