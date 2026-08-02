# core:strings

## Purpose

Single source of Android `R.string` resources for every feature. Translations are pulled
from Phrase by the `pullTranslations` Gradle task (registered via the `sampleplatter.phrase`
convention plugin) and written into `src/androidMain/res/values/` (default locale) and
`values-{locale}/` for translated locales. iOS SwiftUI reads its own
`.lproj/Localizable.strings` files in `iosApp/iosApp/Resources/`, which the same task
generates from the same Phrase project — one source, two native outputs.

## Public API

No Kotlin types, no commonMain code — Android resources only. Consume via the standard
Android resource API:

```kotlin
import com.jkjamies.sampleplatter.core.strings.R

Text(text = stringResource(R.string.menu_title))
```

## Usage

### From a feature presentation module

`sampleplatter.kmp.presentation` adds `core:strings` to every feature's `androidMain`
classpath automatically. No per-feature `build.gradle.kts` change is needed; reference
keys with `stringResource(R.string.…)` from any Compose UI in `androidMain`.

### Pulling latest translations

```bash
./gradlew :core:strings:pullTranslations                # default market
./gradlew :core:strings:pullTranslations -Pmarket=de    # specific market
```

The task requires:

- `phrase.projectId` Gradle property (set in `~/.gradle/gradle.properties` or `local.properties`)
- `PHRASE_API_TOKEN` environment variable, or `phrase.apiToken` Gradle property

## Rules

- **Android only.** No `commonMain`, `iosMain`, or `commonTest` Kotlin code. All
  resources live in `src/androidMain/res/`.
- **Consumed only on `androidMain`.** Features pull this module via the presentation
  convention plugin's `androidMain.dependencies`, never on `commonMain` or `iosMain`,
  so the iOS framework export stays untouched.
- **Never hand-edit translation files.** Any change must come from Phrase via
  `pullTranslations`. Manual edits are overwritten on the next pull.
- **iOS strings live in `iosApp/iosApp/Resources/{locale}.lproj/`** and are written by
  the same Phrase task. SwiftUI reads them via `String(localized: "key")`.
- **No Kotlin code here.** If a `commonMain` string constant is needed, add it to the
  module that owns the logic — do not introduce code into `core:strings`.
