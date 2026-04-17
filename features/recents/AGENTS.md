# Recents Feature

## Business Context
A dedicated screen to display the user's recent activity and orders, accessible from the More menu. Currently a temporary filler screen using placeholder data until the backend API is ready.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| RecentsScreen | api/navigation | Screen (data object) |
| RecentsContent | api/domain | items |
| RecentItem | api/domain | id, name, description, relativeTime, imageUrl |
| GetRecentsContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, RecentsContent> |
| GetRecentsContentImpl | impl/domain | Repository wrapper |
| RecentsRepository | impl/domain -> impl/data | getRecentItems(): Flow<List<RecentItem>> |
| RecentsPresenter | impl/presentation | Collects content, handles back navigation |
| RecentsUiState | impl/presentation | Loading, Empty, Success states with eventSink |
| RecentsEvent | impl/presentation | OnItemTapped, OnBackTapped |

## Cross-Feature Dependencies
- Navigates to: none (OnItemTapped is a no-op for now)
- Imported by: features:more:impl:presentation (navigates here on menu item click)
- Core deps: core:centerpost, core:circuit, core:theme

## Feature-Specific Patterns
- Placeholder hardcoded data in RecentsRepositoryImpl for now.
- Top app bar has a back button which pops the navigator.
- Non-auth-gated screen (Screen, not ProtectedScreen).

## Testing
- Unit: impl/domain/src/commonTest/ (GetRecentsContentImplTest)
- Data: impl/data/src/commonTest/ (RecentsRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (RecentsPresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (RecentsUiTest, RecentsUiRobot, RecentsStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetRecentsContent)
