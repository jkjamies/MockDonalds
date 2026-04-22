import SwiftUI
import ComposeApp

private let tags = RecentsTestTags.shared

struct RecentsView: View {
    let state: RecentsUiState
    @Environment(\.mockDonaldsColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass

    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        Group {
            if let _ = state as? RecentsUiState.Loading {
                VStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: colors.primary))
                    Spacer()
                }
            } else if let _ = state as? RecentsUiState.Empty {
                VStack {
                    Spacer()
                    emptyStateView
                    Spacer()
                }
            } else if let successState = state as? RecentsUiState.Success {
                ScrollView {
                    VStack(spacing: MockDimens.spacingMd) {
                        ForEach(successState.items, id: \.id) { item in
                            RecentItemCard(item: item)
                                .onTapGesture {
                                    successState.eventSink(RecentsEvent.OnItemTapped(id: item.id))
                                }
                        }
                    }
                    .padding(MockDimens.spacingMd)
                    .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
                }
                .accessibilityIdentifier(tags.LIST)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
        .accessibilityIdentifier(tags.SCREEN)
        .navigationTitle("Recents")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var emptyStateView: some View {
        VStack(spacing: MockDimens.spacingLg) {
            Image(systemName: "info.circle")
                .resizable()
                .scaledToFit()
                .frame(width: 64, height: 64)
                .foregroundColor(colors.secondary)

            Text("No recent activity")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(colors.onBackground)

            Text("Your recent orders and items will appear here")
                .font(.body)
                .foregroundColor(colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, MockDimens.spacingXl)
        }
        .accessibilityIdentifier(tags.EMPTY)
    }
}

struct RecentItemCard: View {
    let item: RecentItem
    @Environment(\.mockDonaldsColors) private var colors
    private let tags = RecentsTestTags.shared

    var body: some View {
        HStack(spacing: MockDimens.spacingMd) {
            Group {
                if let urlString = item.imageUrl, let url = URL(string: urlString) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .empty:
                            colors.surfaceContainerHighest
                        case .success(let image):
                            image.resizable().aspectRatio(contentMode: .fill)
                        case .failure:
                            colors.surfaceContainerHighest
                        @unknown default:
                            colors.surfaceContainerHighest
                        }
                    }
                } else {
                    colors.surfaceContainerHighest
                }
            }
            .frame(width: 64, height: 64)
            .cornerRadius(MockDimens.radiusSm)

            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.headline)
                    .foregroundColor(colors.onSurface)

                Text(item.description_)
                    .font(.subheadline)
                    .foregroundColor(colors.onSurfaceVariant)

                Text(item.relativeTime)
                    .font(.caption)
                    .foregroundColor(colors.primary)
            }

            Spacer()
        }
        .padding(MockDimens.spacingMd)
        .background(colors.surfaceContainerHighest)
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier("\(tags.ITEM)-\(item.id)")
    }
}
