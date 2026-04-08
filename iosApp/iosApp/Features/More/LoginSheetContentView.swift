import SwiftUI

struct LoginSheetContentView: View {
    let email: String
    let onEmailChanged: (String) -> Void
    let onSignInClick: () -> Void
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            RoundedRectangle(cornerRadius: 3)
                .fill(colors.surfaceContainerHighest.opacity(0.4))
                .frame(width: 48, height: 6)
                .padding(.vertical, MockDimens.spacingSm)

            Spacer().frame(height: MockDimens.spacingXl)

            Text("MockDonalds")
                .font(.system(size: 32, weight: .black))
                .italic()
                .tracking(-1)
                .foregroundColor(colors.onSurface)

            Spacer().frame(height: MockDimens.spacingXxxl)

            emailField
            Spacer().frame(height: MockDimens.spacingLg)
            signInButton
            Spacer().frame(height: MockDimens.spacingXxl)
            orDivider
            Spacer().frame(height: MockDimens.spacingXxl)
            googleButton
        }
        .padding(.horizontal, MockDimens.spacingXxl)
        .padding(.bottom, MockDimens.spacingXxl)
        .background(colors.surfaceContainerLow)
    }

    private var emailField: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingSm) {
            Text("EMAIL ADDRESS")
                .font(.caption2)
                .fontWeight(.bold)
                .tracking(2)
                .foregroundColor(colors.onSurface.opacity(0.4))
                .padding(.leading, MockDimens.spacingXs)

            TextField(
                "",
                text: Binding(
                    get: { email },
                    set: { onEmailChanged($0) }
                ),
                prompt: Text("gourmet@night.com")
                    .foregroundColor(colors.onSurface.opacity(0.2))
            )
            .keyboardType(.emailAddress)
            .autocapitalization(.none)
            .foregroundColor(colors.onSurface)
            .padding(.horizontal, MockDimens.spacingLg)
            .frame(height: 56)
            .background(colors.surfaceContainerHighest)
            .cornerRadius(MockDimens.radiusMd)
        }
    }

    private var signInButton: some View {
        Button(action: onSignInClick) {
            Text("Sign In")
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
    }

    private var orDivider: some View {
        HStack {
            Rectangle()
                .fill(colors.onSurfaceVariant.opacity(0.2))
                .frame(height: 1)
            Text("OR CONTINUE WITH")
                .font(.caption2)
                .fontWeight(.bold)
                .tracking(3)
                .foregroundColor(colors.onSurface.opacity(0.3))
                .fixedSize()
                .padding(.horizontal, MockDimens.spacingLg)
            Rectangle()
                .fill(colors.onSurfaceVariant.opacity(0.2))
                .frame(height: 1)
        }
    }

    private var googleButton: some View {
        Button(
            action: {},
            label: {
                HStack(spacing: MockDimens.spacingMd) {
                    Text("G")
                        .font(.title3)
                        .foregroundColor(colors.onSurface)
                    Text("GOOGLE")
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
