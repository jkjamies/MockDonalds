import SwiftUI
import ComposeApp

private let tags = WelcomeTestTags.shared

struct WelcomeView: View {
    let state: WelcomeUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Text("M")
                .font(.system(size: 72, weight: .black))
                .italic()
                .foregroundColor(colors.primary)
                .frame(width: 100, height: 100)
                .accessibilityIdentifier(tags.LOGO)

            Spacer().frame(height: MockDimens.spacingXxl)

            Text("Welcome!")
                .font(.system(size: 32, weight: .black))
                .tracking(-1)
                .foregroundColor(colors.onSurface)
                .accessibilityIdentifier(tags.TITLE)

            Spacer().frame(height: MockDimens.spacingMd)

            Text("You're all set")
                .font(.body)
                .foregroundColor(colors.onSurface.opacity(0.6))
                .accessibilityIdentifier(tags.SUBTITLE)

            Spacer()

            Button(
                action: { state.eventSink(WelcomeEvent.ContinueClicked()) },
                label: {
                    Text("Continue")
                        .font(.title3)
                        .fontWeight(.heavy)
                        .tracking(-0.5)
                        .foregroundColor(colors.onPrimaryButton)
                        .frame(maxWidth: .infinity)
                        .frame(height: 64)
                        .background(
                            LinearGradient(
                                colors: [colors.primary, colors.primaryDark],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(MockDimens.radiusMd)
                }
            )
            .accessibilityIdentifier(tags.CONTINUE_BUTTON)

            Spacer().frame(height: MockDimens.spacingXxl)
        }
        .padding(.horizontal, MockDimens.spacingXxl)
        .background(colors.surfaceContainerLow)
        .navigationBarBackButtonHidden(true)
    }
}
