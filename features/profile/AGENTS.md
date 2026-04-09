# Profile Feature

## Business Context
The profile screen displays the authenticated user's account details including name, email, loyalty tier, points balance, avatar, and membership date. It also provides the logout action. This screen is auth-gated -- unauthenticated users are redirected to login before reaching it.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| ProfileScreen | api/navigation | ProtectedScreen (data object), requires authentication |
| ProfileContent | api/domain | name, email, tier, points, avatarUrl, memberSince |
| GetProfileContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, ProfileContent> |
| ProfileRepository | impl/domain -> impl/data | getProfile(): Flow<ProfileContent> |
| ProfilePresenter | impl/presentation | Collects profile content, handles logout via AuthManager |
| ProfileUiState | impl/presentation | name, email, tier, points, avatarUrl, memberSince, eventSink |
| ProfileEvent | impl/presentation | LogoutClicked |

## Cross-Feature Dependencies
- Navigates to: none (LogoutClicked calls authManager.logout() then navigator.pop())
- Imported by: more (MorePresenter navigates to ProfileScreen on ProfileClicked); composeApp (wired at app level)
- Core deps: core:auth:api (AuthManager), core:centerpost, core:theme

## Feature-Specific Patterns
- ProfileScreen implements ProtectedScreen, which triggers auth-gating in the core:circuit layer. Unauthenticated users are redirected to LoginScreen(returnTo=ProfileScreen) before this screen renders.
- This is the only feature using ProtectedScreen -- all other features use Screen or TabScreen.
- On LogoutClicked, the presenter calls `authManager.logout()` and then `navigator.pop()` to return to the previous screen (typically More).
- ProfileUiState fields directly mirror ProfileContent fields (flat mapping, no transformation).

## Testing
- Unit: impl/domain/src/commonTest/ (GetProfileContentImplTest)
- Data: impl/data/src/commonTest/ (ProfileRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (ProfilePresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (ProfileUiTest, ProfileUiRobot, ProfileStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetProfileContent)
