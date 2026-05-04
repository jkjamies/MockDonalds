# Login Feature

## Business Context
The login screen handles user authentication via email, Apple Sign-In, or Google Sign-In. It is not a tab destination but a standalone screen that can be navigated to from any feature requiring auth, with optional return-to navigation after successful login.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| LoginScreen | api/navigation | `FlowScreen` (data class). Has `returnTo: Screen?` parameter. iOS presents as `.fullScreenCover` with inner NavigationStack; Android pushes normally with nested Circuit navigation. |
| LoginContent | api/domain | logoUrl |
| LoginResult | api/domain | success, errorMessage? |
| GetLoginContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, LoginContent> |
| GetLoginContentImpl | impl/domain | Delegates directly to repository.getLoginContent() |
| LoginRepository | impl/domain -> impl/data | getLoginContent(): Flow<LoginContent> |
| LoginPresenter | impl/presentation | Manages email state, auth flow, and return-to navigation |
| LoginUiState | impl/presentation | logoUrl, email, isLoading, errorMessage?, eventSink |
| LoginEvent | impl/presentation | EmailChanged(value), SignInConfirmed, AppleSignInClicked, GoogleSignInClicked |

## Cross-Feature Dependencies
- Navigates to: returns to `screen.returnTo` after login (any Screen passed as parameter)
- Imported by: composeApp (wired at app level); any feature needing auth gates can navigate here via LoginScreen(returnTo=...)
- Core deps: core:auth:api (AuthManager), core:centerpost, core:theme

## Feature-Specific Patterns
- LoginScreen implements `FlowScreen` (from `core:circuit`). On iOS, `BridgeNavigator` detects `FlowScreen` and emits `PresentFlow` instead of `GoTo`, causing `NavigationStateManager` to present it as a `.fullScreenCover` with an inner `NavigationStack`. On Android, `FlowScreen` has no special navigation effect — the screen is pushed normally and uses Circuit's nested `CircuitContent(onNavEvent)` for inner flow navigation.
- LoginScreen is a `data class` (not data object) because it accepts an optional `returnTo: Screen?` parameter for post-login navigation.
- On SignInConfirmed, the presenter calls `authManager.login()`, pops the login screen, then navigates to `returnTo` if provided.
- The presenter uses `rememberSaveable` for email state to survive configuration changes.
- AppleSignInClicked and GoogleSignInClicked are currently no-op centerPost placeholders.
- LoginResult model exists in the API but is not yet consumed by the presenter (future error handling).

## Testing
- Unit: impl/domain/src/commonTest/ (GetLoginContentImplTest)
- Data: impl/data/src/commonTest/ (LoginRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (LoginPresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (LoginUiTest, LoginUiRobot, LoginStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetLoginContent)
