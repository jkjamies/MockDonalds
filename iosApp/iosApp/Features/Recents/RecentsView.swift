import SwiftUI
import ComposeApp

private let tags = RecentsTestTags.shared

struct RecentsView: View {
    let state: RecentsUiState
    @Environment(\.mockDonaldsColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass

    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        VStack {
            navigationBar
            
            if let _ = state as? RecentsUiStateLoading {
                Spacer()
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: colors.primary))
                Spacer()
            } else if let _ = state as? RecentsUiStateEmpty {
                Spacer()
                emptyStateView
                Spacer()
            } else if let successState = state as? RecentsUiStateSuccess {
                ScrollView {
                    VStack(spacing: MockDimens.spacingMd) {
                        ForEach(successState.items, id: \.id) { item in
                            RecentItemCard(item: item)
                                .onTapGesture {
                                    successState.eventSink(RecentsEventOnItemTapped(id: item.id))
                                }
                        }
                    }
                    .padding(MockDimens.spacingMd)
                    .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
                }
                .accessibilityIdentifier(tags.LIST)
            }
        }
        .background(colors.background)
        .accessibilityIdentifier(tags.SCREEN)
        // Hidden navigation bar as we provide our own
        .navigationBarHidden(true)
    }
    
    private var navigationBar: some View {
        HStack {
            Button(action: {
                if let loading = state as? RecentsUiStateLoading {
                    loading.eventSink(RecentsEventOnBackTapped())
                } else if let empty = state as? RecentsUiStateEmpty {
                    empty.eventSink(RecentsEventOnBackTapped())
                } else if let success = state as? RecentsUiStateSuccess {
                    success.eventSink(RecentsEventOnBackTapped())
                }
            }) {
                Image(systemName: "arrow.left")
                    .foregroundColor(colors.onBackground)
            }
            .accessibilityIdentifier(tags.BACK_BUTTON)
            
            Text("Recents")
                .font(.headline)
                .foregroundColor(colors.onBackground)
                .padding(.leading, MockDimens.spacingSm)
            
            Spacer()
        }
        .padding()
        .background(colors.background)
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
        .background(colors.surfaceVariant)
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier("\(tags.ITEM)-\(item.id)")
    }
}