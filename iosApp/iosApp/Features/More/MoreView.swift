import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = MoreTestTags.shared

@CircuitInject(MoreScreen.self, MoreUiState.self)
struct MoreView: View {
    let state: MoreUiState
    @Environment(\.samplePlatterColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass

    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 40) {
                profileSection
                menuList
                joinTeamBanner
            }
            .padding(.horizontal, PlatterDimens.spacingXl)
            .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
        }
        .background(colors.background)
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
                        .foregroundColor(colors.onSurface)
                    Text(
                        "\(profile.tier) \u{2022} \(profile.points)"
                    )
                    .font(.subheadline)
                    .foregroundColor(
                        colors.onSurfaceVariant
                            .opacity(0.8)
                    )
                }
                Spacer()
                Text(">")
                    .foregroundColor(colors.secondary)
            }
            .padding(PlatterDimens.spacingXl)
            .background(colors.surfaceContainerLow)
            .cornerRadius(PlatterDimens.radiusMd)
            .accessibilityElement(children: .contain)
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
                                color: colors.primary,
                                location: 0
                            ),
                            .init(
                                color: colors.secondary,
                                location: 0.45
                            ),
                            .init(
                                color: colors.secondary,
                                location: 0.65
                            ),
                            .init(
                                color: colors.primary,
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
                    colors.surfaceContainerHighest
                }
            )
            .frame(width: 60, height: 60)
            .clipShape(Circle())
        }
    }

    @ViewBuilder
    private var menuList: some View {
        if !state.menuItems.isEmpty {
            VStack(spacing: PlatterDimens.spacingXs) {
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
            .accessibilityElement(children: .contain)
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
            .cornerRadius(PlatterDimens.radiusMd)
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
                colors.surfaceContainerHighest
            }
        )
    }

    private var joinTeamContent: some View {
        VStack(alignment: .leading, spacing: PlatterDimens.spacingLg) {
            Text("Join the Team")
                .font(.title2)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
            Text(
                "Craft the future of late-night dining "
                + "with us. We're looking for culinary "
                + "masters."
            )
            .font(.subheadline)
            .foregroundColor(colors.onSurfaceVariant)

            Button(
                action: {},
                label: {
                    Text("View Openings")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .padding(.horizontal, PlatterDimens.spacingXl)
                        .padding(.vertical, PlatterDimens.spacingMd)
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
                        .cornerRadius(PlatterDimens.radiusSm)
                }
            )
            .padding(.top, PlatterDimens.spacingSm)
        }
        .padding(PlatterDimens.spacingXxl)
    }
}

struct MenuItemView: View {
    let icon: String
    let title: String
    let isOdd: Bool
    @Environment(\.samplePlatterColors) private var colors

    var body: some View {
        HStack {
            HStack(spacing: PlatterDimens.spacingLg) {
                Text(icon)
                    .foregroundColor(
                        colors.onSurfaceVariant
                    )
                Text(title)
                    .font(.body)
                    .fontWeight(.semibold)
                    .foregroundColor(colors.onSurface)
            }
            Spacer()
            Text(">")
                .foregroundColor(
                    colors.onSurfaceVariant
                )
        }
        .padding(20)
        .background(
            isOdd
                ? colors.surface
                : colors.surfaceContainerLow
        )
        .cornerRadius(PlatterDimens.radiusMd)
    }
}
