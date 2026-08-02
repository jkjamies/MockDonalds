# More Feature

## Business Context
The "More" tab serves as a settings and navigation hub, displaying the user's profile summary and a list of menu items for accessing secondary app sections like settings, help, and account management.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| MoreScreen | api/navigation | TabScreen (data object), tag="more" |
| MoreContent | api/domain | userProfile, menuItems |
| UserProfile | api/domain | name, tier, points, avatarUrl |
| MoreMenuItem | api/domain | id, icon, title |
| MoreTabExtension | api/navigation | Extension point for other features to contribute menu entries via Metro multibinding (`@ContributesIntoSet(AppScope::class)`). Fields: `id`, `icon`, `title`, `isDebugOnly` (default `false`), `onClick(Navigator)`. Set `isDebugOnly = true` to have `MorePresenter` hide the entry in release builds (gated on `AppBuildConfig.isDebug`). |
| GetMoreContent | api/domain -> impl/domain | Streaming use case via StrataSubjectInteractor<Unit, MoreContent> |
| MoreRepository | impl/domain -> impl/data | getUserProfile(): Flow<UserProfile>, getMenuItems(): Flow<List<MoreMenuItem>> |
| MorePresenter | impl/presentation | Collects content, merges `Set<MoreTabExtension>` (filtered by `AppBuildConfig.isDebug` for debug-only entries), handles profile navigation and menu item clicks |
| MoreUiState | impl/presentation | userProfile?, menuItems, eventSink |
| MoreEvent | impl/presentation | ProfileClicked, MenuItemClicked(id) |

## Cross-Feature Dependencies
- Navigates to: ProfileScreen (from features:profile:api:navigation) on ProfileClicked
- Imported by: composeApp (wired at app level)
- Core deps: core:build-config:api (runtime `isDebug` gating for MoreTabExtension), core:strata, core:theme
- Extension consumers: any feature contributing a `MoreTabExtension` (e.g. `features:debug-menu`) via `@ContributesIntoSet(AppScope::class)` — no direct dependency from More

## Feature-Specific Patterns
- MoreScreen is a TabScreen with tag="more", making it a bottom navigation destination.
- This is the only feature (besides login) that navigates to another feature's screen: `navigator.goTo(ProfileScreen)` on ProfileClicked.
- `MoreTabExtension` is the extension seam for other features to add menu entries without More having to know about them. Entries with `isDebugOnly = true` are filtered out of the presenter's state when `AppBuildConfig.isDebug` is false — no compile-time stripping needed.
- The build.gradle.kts explicitly depends on `:features:profile:api:navigation` for the cross-feature navigation.

## Testing
- Unit: impl/domain/src/commonTest/ (GetMoreContentImplTest)
- Data: impl/data/src/commonTest/ (MoreRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (MorePresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (MoreUiTest, MoreUiRobot, MoreStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetMoreContent)
