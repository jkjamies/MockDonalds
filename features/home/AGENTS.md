# Home Feature

## Business Context
The home screen is the primary landing tab for authenticated and unauthenticated users. It displays a personalized greeting, a hero promotion banner, recent cravings the user might reorder, and an explore section for discovering new menu areas.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| HomeScreen | api/navigation | TabScreen (data object), tag="home" |
| HomeContent | api/domain | userName, heroPromotion, recentCravings, exploreItems |
| HeroPromotion | api/domain | title, description, tag, imageUrl, ctaText |
| Craving | api/domain | id, title, subtitle, imageUrl |
| ExploreItem | api/domain | id, icon, title, subtitle |
| Promotion | api/domain | id, title, imageUrl (unused in content currently) |
| GetHomeContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, HomeContent> |
| GetHomeContentImpl | impl/domain | Combines 4 repository flows via `combine` |
| HomeRepository | impl/domain -> impl/data | getUserName(), getHeroPromotion(), getRecentCravings(), getExploreItems() -- all Flow-based |
| HomePresenter | impl/presentation | Collects content, maps to UiState, dispatches events via CenterPost |
| HomeUiState | impl/presentation | userName, heroPromotion?, recentCravings, exploreItems, eventSink |
| HomeEvent | impl/presentation | HeroCtaClicked, CravingClicked(id), ExploreItemClicked(id) |

## Cross-Feature Dependencies
- Navigates to: none (event handlers are currently no-ops via centerPost)
- Imported by: composeApp (all feature navigation APIs are wired at app level)
- Core deps: core:centerpost, core:theme

## Feature-Specific Patterns
- HomeScreen is a TabScreen with tag="home", making it one of the bottom navigation destinations.
- The presenter uses `rememberCenterPost(dispatchers)` and `getHomeContent.collectAsState()` for reactive content streaming.
- All event handlers currently delegate to no-op `centerPost { }` blocks -- placeholders for future navigation.
- GetHomeContentImpl combines 4 separate repository flows into a single HomeContent stream.

## Testing
- Unit: impl/domain/src/commonTest/ (GetHomeContentImplTest)
- Data: impl/data/src/commonTest/ (HomeRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (HomePresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (HomeUiTest, HomeUiRobot, HomeStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetHomeContent)
