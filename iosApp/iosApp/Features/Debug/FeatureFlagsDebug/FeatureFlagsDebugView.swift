#if DEBUG
import SwiftUI
import ComposeApp

struct FeatureFlagsDebugView: View {
    let state: FeatureFlagsDebugUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            navigationBar
            Spacer()
            Text("Feature Flags will be listed here")
                .font(.body)
                .foregroundColor(colors.onSurfaceVariant)
            Spacer()
        }
        .background(colors.background)
        .accessibilityIdentifier(FeatureFlagsDebugTestTags.shared.ROOT)
        .navigationBarHidden(true)
    }

    private var navigationBar: some View {
        HStack {
            Button(action: { state.eventSink(FeatureFlagsDebugEvent.BackClicked()) }) {
                Image(systemName: "arrow.left")
                    .foregroundColor(colors.onBackground)
            }

            Text("Feature Flags")
                .font(.headline)
                .foregroundColor(colors.onBackground)
                .padding(.leading, MockDimens.spacingSm)

            Spacer()
        }
        .padding()
        .background(colors.background)
    }
}
#endif
