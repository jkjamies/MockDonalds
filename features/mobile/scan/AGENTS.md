# Scan Feature

## Business Context
The scan screen presents the user's membership QR code for in-store scanning, along with their loyalty status and a compact rewards progress indicator. It enables quick access to mobile payment and current offers.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| ScanScreen | api/navigation | TabScreen (data object), tag="scan" |
| ScanContent | api/domain | memberInfo, rewardsProgress |
| MemberInfo | api/domain | memberStatus, qrCodeUrl |
| ScanRewardsProgress | api/domain | currentPoints, pointsToNextReward, progressFraction, message |
| GetScanContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, ScanContent> |
| ScanRepository | impl/domain -> impl/data | getMemberInfo(): Flow<MemberInfo>, getRewardsProgress(): Flow<ScanRewardsProgress> |
| ScanPresenter | impl/presentation | Collects content, handles pay and offers interactions |
| ScanUiState | impl/presentation | memberInfo?, rewardsProgress?, eventSink |
| ScanEvent | impl/presentation | PayNowClicked, ViewOffersClicked |

## Cross-Feature Dependencies
- Navigates to: none (event handlers are currently no-op centerPost placeholders)
- Imported by: composeApp (wired at app level)
- Core deps: core:centerpost, core:theme

## Feature-Specific Patterns
- ScanScreen is a TabScreen with tag="scan", making it a bottom navigation destination.
- ScanRewardsProgress is a feature-specific type (not shared with the rewards feature) -- it includes a `message` field for scan-context display text.
- MemberInfo contains `qrCodeUrl` which the UI renders as a scannable QR code.
- The domain model is relatively simple with only two data objects combined, reflecting the focused purpose of this screen.
- ScanRewardsProgress includes `progressFraction: Float` for rendering a compact progress indicator.

## Testing
- Unit: impl/domain/src/commonTest/ (GetScanContentImplTest)
- Data: impl/data/src/commonTest/ (ScanRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (ScanPresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (ScanUiTest, ScanUiRobot, ScanStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetScanContent)
