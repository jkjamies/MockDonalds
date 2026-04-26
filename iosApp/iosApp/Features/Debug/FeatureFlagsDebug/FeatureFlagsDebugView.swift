#if DEBUG
import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = FeatureFlagsDebugTestTags.shared

@CircuitInject(FeatureFlagsDebugScreen.self, FeatureFlagsDebugUiState.self)
struct FeatureFlagsDebugView: View {
    let state: FeatureFlagsDebugUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        Group {
            if state.rows.isEmpty {
                VStack {
                    Spacer()
                    Text("No feature flags registered")
                        .font(.body)
                        .foregroundColor(colors.onSurfaceVariant)
                        .accessibilityIdentifier(tags.EMPTY_STATE)
                    Spacer()
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: MockDimens.spacingSm) {
                        ForEach(state.rows, id: \.key) { row in
                            FeatureFlagCard(row: row)
                        }
                    }
                    .padding(MockDimens.spacingMd)
                }
                .accessibilityIdentifier(tags.FLAG_LIST)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
        .accessibilityIdentifier(tags.ROOT)
        .navigationTitle("Feature Flags")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct FeatureFlagCard: View {
    let row: FeatureFlagRow
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(row.key)
                    .font(.headline)
                    .foregroundColor(colors.onSurface)
                if !row.description_.isEmpty {
                    Text(row.description_)
                        .font(.subheadline)
                        .foregroundColor(colors.onSurfaceVariant)
                }
                Text(metaLine)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
            }
            Spacer()
            Toggle("", isOn: .constant(row.enabled))
                .labelsHidden()
                .disabled(true)
        }
        .padding(MockDimens.spacingLg)
        .background(colors.surfaceContainerHighest)
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier("\(tags.FLAG_ROW)-\(row.key)")
    }

    private var metaLine: String {
        if row.owner.isEmpty {
            return row.lifecycle.name
        }
        return "\(row.owner) • \(row.lifecycle.name)"
    }
}
#endif
