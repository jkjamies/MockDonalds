# Naming Conventions Reference

## Full Naming Table

| Type | Pattern | Location | Annotations | Form |
|------|---------|----------|-------------|------|
| Screen | `{Feature}Screen` | `api/navigation` | `@Parcelize` | `data object` |
| Presenter | `{Feature}Presenter` | `impl/presentation` | `@CircuitInject({Screen}::class, AppScope::class)`, `@Inject`, `@Composable` | top-level function |
| UiState | `{Feature}UiState` | `impl/presentation` | none | `data class : CircuitUiState` (must have `eventSink` property) |
| Event | `{Feature}Event` | `impl/presentation` | none | `sealed class` (NOT sealed interface) |
| Use case (abstract) | `Get{Feature}Content` | `api/domain` | none | `abstract class : CenterPostSubjectInteractor<P, T>()` |
| Use case (impl) | `Get{Feature}ContentImpl` | `impl/domain` | `@ContributesBinding(AppScope::class)` | `class` with constructor injection |
| Repository (interface) | `{Feature}Repository` | `impl/domain` | none | `interface` |
| Repository (impl) | `{Feature}RepositoryImpl` | `impl/data` | `@ContributesBinding(AppScope::class)` | `class` with constructor injection |
| DataSource (interface) | `{Feature}RemoteDataSource` / `{Feature}LocalDataSource` | `impl/data/remote/` / `impl/data/local/` | none | `interface` |
| DataSource (impl) | `{Feature}RemoteDataSourceImpl` / `{Feature}LocalDataSourceImpl` | `impl/data/remote/` / `impl/data/local/` | `@ContributesBinding(AppScope::class)` | `class` with constructor injection |
| DTO | `{Name}Dto` | `impl/data/remote/` | `@Serializable` | `data class` |
| Fake | `Fake{Name}` | `test/src/commonMain` | none | `class` extending abstract use case |
| TestTags | `{Feature}TestTags` | `api/navigation` | none | `object` with `const val` tag strings |

## Package Naming Rules

All packages follow strict conventions enforced by `PackageConventionsTest`:

| Module Location | Required Package Prefix |
|----------------|------------------------|
| `features/{name}/**` | `com.mockdonalds.app.features.{name}.*` |
| `core/{module}/**` | `com.mockdonalds.app.core.{module}.*` |

The feature name in the path must match the feature segment in the package. For example, `features/home/api/domain/` must use package `com.mockdonalds.app.features.home.api.domain`.

## Visibility Rules

Enforced by `VisibilityConventionsTest`:

| Type | Visibility | Rationale |
|------|-----------|-----------|
| `@ContributesBinding` classes | `public` (default) | Metro DI resolves them across module boundaries; `internal` would hide them from the DI graph |
| Repository interfaces in impl/domain | `public` | Must be visible to impl/data for implementation |
| `*Impl` classes in impl/domain | `public` | Required for `@ContributesBinding` cross-module resolution |
| UiState data classes | `public` (NOT internal) | They cross module boundaries via Circuit's presenter-to-UI contract |
| Top-level functions in impl/domain | `internal` or `private` | No public utility functions in domain modules |
| All other impl/domain declarations | `internal` or `private` | Only Repository interfaces and DI-bound Impls should be public |

## Correct vs. Incorrect Examples

### Screen
- Correct: `@Parcelize data object HomeScreen : TabScreen`
- Incorrect: `data class HomeScreen(val id: String) : Screen` (screens are singletons, no state)
- Incorrect: `class HomeScreen : Screen` (must be data object)

### Event
- Correct: `sealed class HomeEvent { data object HeroCtaClicked : HomeEvent() }`
- Incorrect: `sealed interface HomeEvent { data object HeroCtaClicked : HomeEvent }` (breaks iOS Obj-C export)

### Use Case
- Correct abstract: `abstract class GetHomeContent : CenterPostSubjectInteractor<Unit, HomeContent>()`
- Correct impl: `@ContributesBinding(AppScope::class) class GetHomeContentImpl(...) : GetHomeContent()`
- Incorrect: `class GetHomeContent` (abstract missing), `GetHomeContentImplementation` (must use `Impl` suffix)

### Repository
- Correct: `interface HomeRepository` in impl/domain, `@ContributesBinding(AppScope::class) class HomeRepositoryImpl : HomeRepository` in impl/data
- Incorrect: `class HomeRepo` (must end with `Repository`), `HomeRepositoryImplementation` (must use `Impl` suffix)

### DataSource
- Correct: `interface MenuRemoteDataSource` in `impl/data/remote/`, `@ContributesBinding(AppScope::class) class MenuRemoteDataSourceImpl(...) : MenuRemoteDataSource` in `impl/data/remote/`
- Correct: `interface MenuLocalDataSource` in `impl/data/local/`, `class MenuLocalDataSourceImpl(...) : MenuLocalDataSource` in `impl/data/local/`
- Incorrect: `MenuDataSource` (must specify Remote or Local), `MenuRemoteDataSource` in `impl/data/` root (must be in `remote/` package)

### DTO
- Correct: `@Serializable data class MenuItemDto(...)` in `impl/data/remote/`
- Incorrect: `data class MenuItemResponse(...)` (must use `Dto` suffix), `MenuItemDto` in `impl/data/` root (must be in `remote/` package), `MenuItemDto` without `@Serializable`

## Rationale for Key Decisions

| Decision | Why |
|----------|-----|
| `data object` for Screens | Screens are singletons with no instance state; data object gives structural equality, `toString()`, and zero allocation overhead |
| `sealed class` (not interface) for Events | Kotlin sealed interfaces export as Obj-C protocols in iOS, which breaks `Event.Subtype()` constructor syntax in Swift. Sealed classes export as proper Obj-C classes with subtype constructors |
| `Impl` suffix for implementations | Konsist enforces this pattern (`NamingConventionsTest`), provides instant visual clarity about which class is the contract vs. implementation, and enables automated DI pairing checks in `DependencyInjectionTest` |
| `Fake` prefix for test doubles | Distinguishes test infrastructure from production code; Konsist `TestDoubleConventionsTest` enforces the prefix |

## Konsist Test Classes Enforcing Naming

| Rule | Enforced By |
|------|-------------|
| Screen/Presenter/UiState/Event suffixes | `NamingConventionsTest` |
| UseCase abstract vs Impl naming | `NamingConventionsTest` |
| Repository/RepositoryImpl naming | `NamingConventionsTest` |
| Events are sealed class not interface | `CircuitConventionsTest` |
| Screens in api/navigation with @Parcelize | `CircuitConventionsTest` |
| Package prefix conventions | `PackageConventionsTest` |
| Visibility of @ContributesBinding, UiState, domain types | `VisibilityConventionsTest` |
| Test file naming (Fake prefix) | `TestDoubleConventionsTest`, `TestFileNamingTest` |
| DataSource naming + location (remote/ and local/ packages) | `DataLayerTest` |
| DTO naming (*Dto suffix), @Serializable, location (remote/ package in impl/data) | `DataLayerTest` |
| Network import restriction (only impl/data + composeApp) | `LayerDependencyTest` |
