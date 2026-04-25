# CircuitMacros (Swift Macro Plugin)

## Purpose

Defines the `@CircuitInject` Swift macro consumed by every SwiftUI feature view in `iosApp/iosApp/Features/`. The macro is the iOS counterpart to Kotlin's `@CircuitInject` on Compose `Ui` functions — both annotate the renderer for a `Screen` → `UiState` pair and let auto-discovery wire it into the navigator.

## Package Layout

```
iosApp/CircuitMacros/
├── Package.swift                      # SPM manifest (swift-tools-version: 6.0)
└── Sources/
    ├── CircuitMacros/
    │   └── CircuitInject.swift        # Public attribute declaration (re-exports the plugin)
    └── CircuitMacrosPlugin/
        ├── CircuitInjectMacro.swift   # PeerMacro implementation (returns [])
        └── Plugin.swift               # CompilerPlugin entry point (@main)
```

| Target | Kind | Purpose |
|---|---|---|
| `CircuitMacros` | `.target` | Public-facing module imported by SwiftUI views (`import CircuitMacros`). Holds the `@attached(peer)` macro declaration that points at the plugin. |
| `CircuitMacrosPlugin` | `.macro` | The compiler plugin itself. Provides `CircuitInjectMacro: PeerMacro` and the `@main CompilerPlugin` shell. Depends on `SwiftSyntax`, `SwiftSyntaxMacros`, `SwiftCompilerPlugin` from `swift-syntax` 600.0.0+. |

## How It Works

`@CircuitInject(HomeScreen.self, HomeUiState.self)` is a **marker peer macro that emits no code** (the implementation returns `[]`). Its only job is compile-time validation that the referenced KMP types resolve in scope — if a developer mistypes `HomeScren.self`, the Swift compiler fails the build immediately.

The actual factory-table generation is done by `CircuitFactoryRegistry` (see `build-tooling/CircuitFactoryRegistry/AGENTS.md`), a separate SwiftSyntax-based codegen tool that walks the source tree at build time and emits `iosApp/iosApp/Generated/GeneratedCircuitFactories.swift`.

## Why Two Packages?

The macro plugin and the codegen tool live in separate SPM packages because:

1. **`CircuitMacros` is consumed by the iOS app target** (iOS device + simulator builds) and must be linkable into iOS-targeted code.
2. **`CircuitFactoryRegistry` is a macOS executable** invoked from a Run Script Phase. It cannot live in `iosApp/` because Xcode's iOS build environment leaks `SDKROOT=iphonesimulator` into nested `swift build` invocations, breaking macOS-only manifest evaluation. Keeping the registry under `build-tooling/` (outside the iOS project root) plus an explicit `unset SDKROOT …` in the build script keeps SwiftPM happy.

## Why `[.macOS(.v14), .iOS(.v16)]` in `Package.swift`?

Even though the macro is consumed only by iOS code, **macros themselves are macOS host tools** — they run at compile time on the developer's Mac. The `.macOS(.v14)` floor is required for SwiftSyntax 600. The `.iOS(.v16)` floor matches the iosApp deployment target so the public `CircuitMacros` library can link into iOS targets.

## Usage

```swift
import CircuitMacros

@CircuitInject(HomeScreen.self, HomeUiState.self)
struct HomeView: View {
    let state: HomeUiState
    var body: some View { /* ... */ }
}
```

The annotated view must expose `init(state: SomeUiState)` — `CircuitFactoryRegistry` emits `ScreenUiFactory<HomeScreen, HomeUiState> { HomeView(state: $0) }`, calling that initializer directly.

`#if DEBUG`-wrapped views are auto-detected by the `CircuitFactoryRegistry` visitor and gated in the generated registry; no explicit `debug:` argument is needed on the macro.

## Why Peer Macro?

A `PeerMacro` is the lightest macro flavor — it doesn't modify the annotated declaration, it just attaches alongside it. Returning `[]` means no peer is actually generated; the type-resolution side effect of having the compiler resolve `HomeScreen.self` and `HomeUiState.self` at parse time is the entire value proposition. We deliberately do not use `MemberMacro`/`ExtensionMacro` patterns that would produce per-view boilerplate, because that's what `CircuitFactoryRegistry` does in one centralized pass.

## Build & Verify

```bash
cd iosApp/CircuitMacros && swift build       # Builds the macro plugin
```

The macro is verified end-to-end every time `xcodebuild` compiles `iosApp` — any annotated view with type errors fails the iOS build.

## Reference

- `.agents/standards/ios-interop.md` — full `@CircuitInject` + registry workflow including the SDKROOT env hygiene gotcha
- `build-tooling/CircuitFactoryRegistry/AGENTS.md` — the codegen tool that consumes `@CircuitInject` annotations
