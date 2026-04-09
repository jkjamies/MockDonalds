import SwiftUI
import ComposeApp

private let tags = ProfileTestTags.shared

struct ProfileView: View {
    let state: ProfileUiState
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: MockDimens.spacingXl) {
                Spacer().frame(height: MockDimens.spacingXl)

                avatar

                VStack(spacing: MockDimens.spacingSm) {
                    Text(state.name)
                        .font(.title)
                        .fontWeight(.black)
                        .foregroundColor(colors.onSurface)
                        .accessibilityIdentifier(tags.NAME)

                    Text(state.email)
                        .font(.subheadline)
                        .foregroundColor(colors.onSurfaceVariant)
                        .accessibilityIdentifier(tags.EMAIL)
                }

                Text("\(state.tier) \u{2022} \(state.points)")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(colors.primary)
                    .accessibilityIdentifier(tags.TIER_POINTS)

                Text(state.memberSince)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
                    .accessibilityIdentifier(tags.MEMBER_SINCE)

                Spacer().frame(height: MockDimens.spacingXl)

                Button(
                    action: {
                        state.eventSink(ProfileEvent.LogoutClicked())
                    },
                    label: {
                        Text("Log Out")
                            .font(.body)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, MockDimens.spacingMd)
                            .background(colors.primary)
                            .cornerRadius(MockDimens.radiusMd)
                    }
                )
                .accessibilityIdentifier(tags.LOGOUT_BUTTON)
            }
            .padding(.horizontal, MockDimens.spacingXxl)
        }
        .background(colors.background)
    }

    private var avatar: some View {
        AsyncImage(
            url: URL(string: state.avatarUrl),
            content: { image in
                image.resizable()
                    .aspectRatio(contentMode: .fill)
            },
            placeholder: {
                colors.surfaceContainerHighest
            }
        )
        .frame(width: 120, height: 120)
        .clipShape(Circle())
        .accessibilityIdentifier(tags.AVATAR)
    }
}
