# Rewards Feature

## Business Context
The rewards screen shows the user's loyalty program status, including points progress toward the next reward, a vault of redeemable specials, and a history of points earned and spent. Users can browse vault items and view their full rewards history.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| RewardsScreen | api/navigation | TabScreen (data object), tag="rewards" |
| RewardsContent | api/domain | progress, vaultSpecials, history |
| RewardsProgress | api/domain | currentPoints, nextRewardName, pointsToNextReward, progressFraction |
| VaultSpecial | api/domain | id, title, pointsCost, imageUrl, tag?, isFeatured |
| HistoryEntry | api/domain | id, title, subtitle, points, isPositive, icon |
| GetRewardsContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, RewardsContent> |
| RewardsRepository | impl/domain -> impl/data | getRewardsProgress(), getVaultSpecials(), getHistory() -- all Flow-based |
| RewardsPresenter | impl/presentation | Collects content, handles vault and history interactions |
| RewardsUiState | impl/presentation | progress?, vaultSpecials, history, eventSink |
| RewardsEvent | impl/presentation | VaultSpecialClicked(id), ViewAllClicked |

## Cross-Feature Dependencies
- Navigates to: none (event handlers are currently no-op centerPost placeholders)
- Imported by: composeApp (wired at app level)
- Core deps: core:centerpost, core:theme

## Feature-Specific Patterns
- RewardsScreen is a TabScreen with tag="rewards", making it a bottom navigation destination.
- RewardsProgress includes a `progressFraction: Float` for rendering progress bars in the UI.
- VaultSpecial has `isFeatured` and optional `tag` fields for visual emphasis on promoted rewards.
- HistoryEntry uses `isPositive: Boolean` to distinguish between earned and spent points.
- The domain model is the richest after order, with three distinct data collections combined.

## Testing
- Unit: impl/domain/src/commonTest/ (GetRewardsContentImplTest)
- Data: impl/data/src/commonTest/ (RewardsRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (RewardsPresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (RewardsUiTest, RewardsUiRobot, RewardsStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetRewardsContent)
