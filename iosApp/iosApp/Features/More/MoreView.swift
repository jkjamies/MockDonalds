import SwiftUI
import ComposeApp

private let tags = MoreTestTags.shared

struct MoreView: View {
    let state: MoreUiState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 40) {
                profileSection
                menuList
                joinTeamBanner
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }

    @ViewBuilder
    private var profileSection: some View {
        if let profile = state.userProfile {
            HStack(spacing: 20) {
                profileAvatar(profile: profile)

                VStack(alignment: .leading) {
                    Text(profile.name)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                    Text(
                        "\(profile.tier) \u{2022} \(profile.points)"
                    )
                    .font(.subheadline)
                    .foregroundColor(
                        MockDonaldsColors.onSurfaceVariant
                            .opacity(0.8)
                    )
                }
                Spacer()
                Text(">")
                    .foregroundColor(MockDonaldsColors.secondary)
            }
            .padding(24)
            .background(MockDonaldsColors.surfaceContainerLow)
            .cornerRadius(12)
            .accessibilityIdentifier(tags.PROFILE_SECTION)
            .onTapGesture {
                state.eventSink(MoreEvent.ProfileClicked())
            }
        }
    }

    private func profileAvatar(
        profile: UserProfile
    ) -> some View {
        ZStack {
            Circle()
                .strokeBorder(
                    AngularGradient(
                        gradient: Gradient(stops: [
                            .init(
                                color: MockDonaldsColors.primary,
                                location: 0
                            ),
                            .init(
                                color: MockDonaldsColors.secondary,
                                location: 0.45
                            ),
                            .init(
                                color: MockDonaldsColors.secondary,
                                location: 0.65
                            ),
                            .init(
                                color: MockDonaldsColors.primary,
                                location: 1
                            ),
                        ]),
                        center: .center
                    ),
                    lineWidth: 3
                )
                .frame(width: 68, height: 68)

            AsyncImage(
                url: URL(string: profile.avatarUrl),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fill)
                },
                placeholder: {
                    MockDonaldsColors.surfaceContainerHighest
                }
            )
            .frame(width: 60, height: 60)
            .clipShape(Circle())
        }
    }

    @ViewBuilder
    private var menuList: some View {
        if !state.menuItems.isEmpty {
            VStack(spacing: 4) {
                ForEach(
                    Array(state.menuItems.enumerated()),
                    id: \.offset
                ) { index, item in
                    MenuItemView(
                        icon: item.icon,
                        title: item.title,
                        isOdd: index % 2 == 0
                    )
                    .accessibilityIdentifier(
                        "\(tags.MENU_ITEM)-\(item.id)"
                    )
                    .onTapGesture {
                        state.eventSink(
                            MoreEvent.MenuItemClicked(id: item.id)
                        )
                    }
                }
            }
            .accessibilityIdentifier(tags.MENU_LIST)
        }
    }

    private var joinTeamBanner: some View {
        Color.clear
            .frame(height: 220)
            .overlay {
                joinTeamImage
            }
            .clipped()
            .overlay {
                LinearGradient(
                    colors: [
                        Color.black,
                        Color.black.opacity(0.4),
                        .clear,
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            }
            .overlay(alignment: .leading) {
                joinTeamContent
            }
            .cornerRadius(12)
            .accessibilityIdentifier(tags.JOIN_TEAM_BANNER)
    }

    private var joinTeamImage: some View {
        // swiftlint:disable:next line_length
        let imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtoKS4itUpfiQzJW9FGblMq9_3wzFqLR5CaS2eM929pYK-KWYvYqQiXcGfWz8ZVUlPcU1hmo0qseeHENBB_sP17bYCskdZ9VPfrIdYy7P63B5tGH6kgBQmn_i0RAanG3-y3r2F2U9G7IdqC5pgPPtd0CVRV-7jjEKtk7VGHqiwH40htvVQRSEZSqoJZ0hnlFw0FvqVNCM5k7pn_eI5N9zunkr86XGaaEl2qddd7Zld_sJOFnulnp_tJ8eqVDNAqvGdId-JcKf1t2s"
        return AsyncImage(
            url: URL(string: imageUrl),
            content: { image in
                image.resizable()
                    .aspectRatio(contentMode: .fill)
            },
            placeholder: {
                MockDonaldsColors.surfaceContainerHighest
            }
        )
    }

    private var joinTeamContent: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Join the Team")
                .font(.title2)
                .fontWeight(.black)
                .foregroundColor(Color(hex: 0xF5F5F5))
            Text(
                "Craft the future of late-night dining "
                + "with us. We're looking for culinary "
                + "masters."
            )
            .font(.subheadline)
            .foregroundColor(Color(hex: 0xD4D4D4))

            Button(
                action: {},
                label: {
                    Text("View Openings")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(
                            LinearGradient(
                                colors: [
                                    MockDonaldsColors.primary,
                                    Color(hex: 0x930003),
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(6)
                }
            )
            .padding(.top, 8)
        }
        .padding(32)
    }
}

struct MenuItemView: View {
    let icon: String
    let title: String
    let isOdd: Bool

    var body: some View {
        HStack {
            HStack(spacing: 16) {
                Text(icon)
                    .foregroundColor(
                        MockDonaldsColors.onSurfaceVariant
                    )
                Text(title)
                    .font(.body)
                    .fontWeight(.semibold)
                    .foregroundColor(MockDonaldsColors.onSurface)
            }
            Spacer()
            Text(">")
                .foregroundColor(
                    MockDonaldsColors.onSurfaceVariant
                )
        }
        .padding(20)
        .background(
            isOdd
                ? MockDonaldsColors.surface
                : MockDonaldsColors.surfaceContainerLow
        )
        .cornerRadius(12)
    }
}
