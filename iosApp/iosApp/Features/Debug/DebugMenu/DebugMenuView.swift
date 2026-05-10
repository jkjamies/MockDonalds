#if DEBUG
import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = DebugMenuTestTags.shared

@CircuitInject(DebugMenuScreen.self, DebugMenuUiState.self)
struct DebugMenuView: View {
    let state: DebugMenuUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: MockDimens.spacingMd) {
                ForEach(state.entries, id: \.id) { entry in
                    DebugEntryCard(entry: entry)
                        .onTapGesture {
                            state.eventSink(DebugMenuEvent.EntryClicked(id: entry.id))
                        }
                }
            }
            .padding(MockDimens.spacingMd)
            .accessibilityIdentifier(tags.ENTRY_LIST)
        }
        .background(colors.background)
        .accessibilityIdentifier(tags.ROOT)
        .navigationTitle("Debug Menu")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct DebugEntryCard: View {
    let entry: DebugMenuEntry
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(entry.title)
                    .font(.headline)
                    .foregroundColor(colors.onSurface)
                Text(entry.subtitle)
                    .font(.subheadline)
                    .foregroundColor(colors.onSurfaceVariant)
            }
            Spacer()
            Text(">")
                .foregroundColor(colors.onSurfaceVariant)
        }
        .padding(MockDimens.spacingLg)
        .background(colors.surfaceContainerHighest)
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier("\(DebugMenuTestTags.shared.ENTRY_ITEM)-\(entry.id)")
    }
}
#endif
