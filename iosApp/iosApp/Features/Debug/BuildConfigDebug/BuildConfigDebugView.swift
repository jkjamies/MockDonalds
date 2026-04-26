#if DEBUG
import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = BuildConfigDebugTestTags.shared

@CircuitInject(BuildConfigDebugScreen.self, BuildConfigDebugUiState.self)
struct BuildConfigDebugView: View {
    let state: BuildConfigDebugUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: MockDimens.spacingSm) {
                ForEach(BuildConfigFieldGroup.allCases, id: \.self) { group in
                    let fieldsInGroup = state.fields.filter { $0.group == group.kotlinValue }
                    if !fieldsInGroup.isEmpty {
                        Text(group.displayName)
                            .font(.subheadline)
                            .foregroundColor(colors.onSurfaceVariant)
                            .padding(.top, MockDimens.spacingMd)
                            .accessibilityIdentifier("\(tags.GROUP_HEADER)-\(group.displayName)")
                        ForEach(fieldsInGroup, id: \.name) { field in
                            FieldRow(field: field)
                        }
                    }
                }
            }
            .padding(MockDimens.spacingMd)
        }
        .accessibilityIdentifier(tags.FIELD_LIST)
        .background(colors.background)
        .accessibilityIdentifier(tags.ROOT)
        .navigationTitle("Build Config")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct FieldRow: View {
    let field: BuildConfigField
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(field.name)
                    .font(.subheadline)
                    .foregroundColor(colors.onSurfaceVariant)
                Text(field.value.isEmpty ? "—" : field.value)
                    .font(.body)
                    .foregroundColor(colors.onSurface)
            }
            Spacer()
            Text("read-only")
                .font(.caption)
                .foregroundColor(colors.onSurfaceVariant)
        }
        .padding(MockDimens.spacingLg)
        .background(colors.surfaceContainerHighest.opacity(0.6))
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier("\(tags.FIELD_ROW)-\(field.name)")
    }
}

private enum BuildConfigFieldGroup: CaseIterable {
    case identity, urls, localization

    var displayName: String {
        switch self {
        case .identity: return "Identity"
        case .urls: return "Urls"
        case .localization: return "Localization"
        }
    }

    var kotlinValue: BuildConfigField.Group {
        switch self {
        case .identity: return BuildConfigField.Group.identity
        case .urls: return BuildConfigField.Group.urls
        case .localization: return BuildConfigField.Group.localization
        }
    }
}
#endif
