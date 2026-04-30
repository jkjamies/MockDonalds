import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = NutritionTestTags.shared

@CircuitInject(NutritionScreen.self, NutritionUiState.self)
struct NutritionView: View {
    let state: NutritionUiState
    @Environment(\.mockDonaldsColors) private var colors
    @State private var canGoBack: Bool = false

    var body: some View {
        Group {
            if let urlString = state.url, let url = URL(string: urlString) {
                WebView(url: url, allowJs: true, canGoBack: $canGoBack)
                    .accessibilityIdentifier(tags.WEBVIEW)
            } else {
                VStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: colors.primary))
                    Spacer()
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
        .accessibilityIdentifier(tags.SCREEN)
        .navigationTitle("Nutrition")
        .navigationBarTitleDisplayMode(.inline)
    }
}
