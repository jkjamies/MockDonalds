import SwiftUI
import ComposeApp

private let tags = LoginTestTags.shared

struct LoginView: View {
    let state: LoginUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                dragHandle
                brandingSection
                loginForm
                orDivider
                socialButtons
                Spacer().frame(height: MockDimens.spacingXxl)
            }
            .padding(.horizontal, MockDimens.spacingXxl)
        }
        .background(colors.surfaceContainerLow)
    }

    private var dragHandle: some View {
        RoundedRectangle(cornerRadius: 3)
            .fill(colors.surfaceContainerHighest.opacity(0.4))
            .frame(width: 48, height: 6)
            .padding(.vertical, MockDimens.spacingSm)
            .accessibilityIdentifier(tags.DRAG_HANDLE)
    }

    private var brandingSection: some View {
        VStack(spacing: MockDimens.spacingLg) {
            AsyncImage(
                url: URL(string: state.logoUrl),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fit)
                },
                placeholder: {
                    colors.surfaceContainerHighest
                }
            )
            .frame(width: 64, height: 64)

            Text("MockDonalds")
                .font(.system(size: 32, weight: .black))
                .italic()
                .tracking(-1)
                .foregroundColor(colors.onSurface)
        }
        .padding(.top, MockDimens.spacingXxl)
        .padding(.bottom, MockDimens.spacingXxxl)
        .accessibilityIdentifier(tags.BRANDING)
    }

    private var loginForm: some View {
        VStack(spacing: MockDimens.spacingLg) {
            emailField

            Spacer().frame(height: MockDimens.spacingSm)

            signInButton
        }
    }

    private var emailField: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingSm) {
            Text("EMAIL ADDRESS")
                .font(.caption2)
                .fontWeight(.bold)
                .tracking(2)
                .foregroundColor(
                    colors.onSurface.opacity(0.4)
                )
                .padding(.leading, MockDimens.spacingXs)

            TextField(
                "",
                text: Binding(
                    get: { state.email },
                    set: {
                        state.eventSink(
                            LoginEvent.EmailChanged(value: $0)
                        )
                    }
                ),
                prompt: Text("gourmet@night.com")
                    .foregroundColor(
                        colors.onSurface.opacity(0.2)
                    )
            )
            .keyboardType(.emailAddress)
            .autocapitalization(.none)
            .foregroundColor(colors.onSurface)
            .padding(.horizontal, MockDimens.spacingLg)
            .frame(height: 56)
            .background(colors.surfaceContainerHighest)
            .cornerRadius(MockDimens.radiusMd)
            .accessibilityIdentifier(tags.EMAIL_INPUT)
        }
    }

    private var signInButton: some View {
        Button(
            action: {
                state.eventSink(LoginEvent.SignInClicked())
            },
            label: {
                Text("Sign In")
                    .font(.title3)
                    .fontWeight(.heavy)
                    .tracking(-0.5)
                    .foregroundColor(colors.onPrimaryButton)
                    .frame(maxWidth: .infinity)
                    .frame(height: 64)
                    .background(
                        LinearGradient(
                            colors: [
                                colors.primary,
                                colors.primaryDark,
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(MockDimens.radiusMd)
            }
        )
        .accessibilityIdentifier(tags.SIGN_IN_BUTTON)
    }

    private var orDivider: some View {
        HStack {
            Rectangle()
                .fill(
                    colors.onSurfaceVariant.opacity(0.2)
                )
                .frame(height: 1)
            Text("OR CONTINUE WITH")
                .font(.caption2)
                .fontWeight(.bold)
                .tracking(3)
                .foregroundColor(
                    colors.onSurface.opacity(0.3)
                )
                .fixedSize()
                .padding(.horizontal, MockDimens.spacingLg)
            Rectangle()
                .fill(
                    colors.onSurfaceVariant.opacity(0.2)
                )
                .frame(height: 1)
        }
        .padding(.vertical, MockDimens.spacingXxxl)
    }

    private var socialButtons: some View {
        HStack(spacing: MockDimens.spacingLg) {
            SocialLoginButton(
                icon: "\u{F8FF}",
                label: "APPLE",
                action: {
                    state.eventSink(
                        LoginEvent.AppleSignInClicked()
                    )
                }
            )
            .accessibilityIdentifier(tags.APPLE_BUTTON)

            SocialLoginButton(
                icon: "G",
                label: "GOOGLE",
                action: {
                    state.eventSink(
                        LoginEvent.GoogleSignInClicked()
                    )
                }
            )
            .accessibilityIdentifier(tags.GOOGLE_BUTTON)
        }
    }
}

struct SocialLoginButton: View {
    @Environment(\.mockDonaldsColors) private var colors
    let icon: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(
            action: action,
            label: {
                HStack(spacing: MockDimens.spacingMd) {
                    Text(icon)
                        .font(.title3)
                        .foregroundColor(colors.onSurface)
                    Text(label)
                        .font(.caption2)
                        .fontWeight(.bold)
                        .tracking(2)
                        .foregroundColor(colors.onSurface)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(colors.surfaceContainerHighest)
                .cornerRadius(MockDimens.radiusMd)
            }
        )
    }
}
