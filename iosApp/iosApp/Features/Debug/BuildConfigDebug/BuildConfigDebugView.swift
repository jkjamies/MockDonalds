#if DEBUG
import SwiftUI
import ComposeApp

struct BuildConfigDebugView: View {
    let state: BuildConfigDebugUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            navigationBar
            Spacer()
            Text("Build Config details will be listed here")
                .font(.body)
                .foregroundColor(colors.onSurfaceVariant)
            Spacer()
        }
        .background(colors.background)
        .accessibilityIdentifier(BuildConfigDebugTestTags.shared.ROOT)
        .navigationBarHidden(true)
    }

    private var navigationBar: some View {
        HStack {
            Button(action: { state.eventSink(BuildConfigDebugEvent.BackClicked()) }) {
                Image(systemName: "arrow.left")
                    .foregroundColor(colors.onBackground)
            }

            Text("Build Config")
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
