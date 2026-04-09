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
| GetMoreContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, MoreContent> |
| MoreRepository | impl/domain -> impl/data | getUserProfile(): Flow<UserProfile>, getMenuItems(): Flow<List<MoreMenuItem>> |
| MorePresenter | impl/presentation | Collects content, handles profile navigation and menu item clicks |
| MoreUiState | impl/presentation | userProfile?, menuItems, eventSink |
| MoreEvent | impl/presentation | ProfileClicked, MenuItemClicked(id) |

## Cross-Feature Dependencies
- Navigates to: ProfileScreen (from features:profile:api:navigation) on ProfileClicked
- Imported by: composeApp (wired at app level)
- Core deps: core:centerpost, core:theme

## Feature-Specific Patterns
- MoreScreen is a TabScreen with tag="more", making it a bottom navigation destination.
- This is the only feature (besides login) that navigates to another feature's screen: `navigator.goTo(ProfileScreen)` on ProfileClicked.
- MenuItemClicked events are currently no-ops -- future expansion points for settings, help, etc.
- The build.gradle.kts explicitly depends on `:features:profile:api:navigation` for the cross-feature navigation.

## Testing
- Unit: impl/domain/src/commonTest/ (GetMoreContentImplTest)
- Data: impl/data/src/commonTest/ (MoreRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (MorePresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (MoreUiTest, MoreUiRobot, MoreStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetMoreContent)
