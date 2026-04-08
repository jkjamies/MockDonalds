import SwiftUI
import ComposeApp

private let tags = HomeTestTags.shared

struct HomeView: View {
    @Environment(\.mockDonaldsColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    let state: HomeUiState

    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: MockDimens.spacingXxxl) {
                greetingSection
                heroBanner
                recentCravingsSection
                exploreSection
                Spacer().frame(height: MockDimens.spacingXl)
            }
            .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
        }
        .background(colors.background)
    }

    private var greetingSection: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingXs) {
            Text("GOOD EVENING, GOURMET")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(colors.onSurfaceVariant)
            Text(state.userName)
                .font(.largeTitle)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
                .accessibilityIdentifier(tags.USER_NAME)
        }
        .padding(.horizontal, MockDimens.spacingXl)
    }

    @ViewBuilder
    private var heroBanner: some View {
        if let hero = state.heroPromotion {
            Color.clear
                .frame(height: MockDimens.adaptiveHeroHeight(isLandscape: isLandscape))
                .overlay {
                    AsyncImage(
                        url: URL(string: hero.imageUrl),
                        content: { image in
                            image.resizable()
                                .aspectRatio(contentMode: .fill)
                        },
                        placeholder: {
                            colors.surfaceContainerHigh
                        }
                    )
                }
                .clipped()
                .overlay {
                    LinearGradient(
                        colors: [
                            .clear,
                            colors.background.opacity(0.9),
                        ],
                        startPoint: .top, endPoint: .bottom
                    )
                }
                .overlay {
                    LinearGradient(
                        colors: [
                            colors.background.opacity(0.6),
                            .clear,
                        ],
                        startPoint: .leading, endPoint: .trailing
                    )
                }
                .accessibilityIdentifier(tags.HERO_BANNER)
                .overlay(alignment: .bottomLeading) {
                    heroOverlayContent(hero: hero)
                }
        }
    }

    private func heroOverlayContent(
        hero: HeroPromotion
    ) -> some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingLg) {
            Text(hero.tag)
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(colors.onSecondaryTag)
                .padding(.horizontal, MockDimens.spacingMd)
                .padding(.vertical, MockDimens.spacingXs)
                .background(colors.secondary)
                .clipShape(Capsule())

            Text(hero.title)
                .font(.system(size: 36, weight: .black))
                .foregroundColor(colors.onSurface)
                .lineSpacing(-4)

            Text(hero.description_)
                .font(.subheadline)
                .foregroundColor(
                    colors.onSurface.opacity(0.7)
                )

            Button(
                action: {
                    state.eventSink(HomeEvent.HeroCtaClicked())
                },
                label: {
                    Text(hero.ctaText)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onPrimaryButton)
                    .padding(.horizontal, MockDimens.spacingXxl)
                    .padding(.vertical, MockDimens.spacingLg)
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
                    .cornerRadius(MockDimens.radiusSm)
                }
            )
            .accessibilityIdentifier(tags.HERO_CTA_BUTTON)
            .padding(.top, MockDimens.spacingLg)
        }
        .padding(MockDimens.spacingXxl)
    }

    @ViewBuilder
    private var recentCravingsSection: some View {
        if !state.recentCravings.isEmpty {
            VStack(alignment: .leading, spacing: MockDimens.spacingXl) {
                HStack {
                    Text("Recent Cravings")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                    Spacer()
                    Text("View All")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(colors.secondary)
                }
                .padding(.horizontal, MockDimens.spacingXl)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: MockDimens.spacingXl) {
                        ForEach(
                            Array(state.recentCravings.enumerated()),
                            id: \.offset
                        ) { _, craving in
                            CravingCard(
                                title: craving.title,
                                subtitle: craving.subtitle,
                                imageUrl: craving.imageUrl,
                                onTap: {
                                    state.eventSink(
                                        HomeEvent.CravingClicked(
                                            id: craving.id
                                        )
                                    )
                                }
                            )
                            .accessibilityIdentifier(
                                "\(tags.CRAVING_CARD)-\(craving.id)"
                            )
                        }
                    }
                    .padding(.horizontal, MockDimens.spacingXl)
                }
            }
            .accessibilityIdentifier(tags.RECENT_CRAVINGS_SECTION)
        }
    }

    @ViewBuilder
    private var exploreSection: some View {
        if !state.exploreItems.isEmpty {
            VStack(alignment: .leading, spacing: MockDimens.spacingXl) {
                Text("Explore")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onSurface)

                exploreBentoGrid
                exploreListItems
            }
            .padding(.horizontal, MockDimens.spacingXl)
            .accessibilityIdentifier(tags.EXPLORE_SECTION)
        }
    }

    private var exploreBentoGrid: some View {
        let gridCount = isLandscape ? 3 : 2
        let gridItems = Array(state.exploreItems.prefix(gridCount))
        return HStack(spacing: MockDimens.spacingLg) {
            ForEach(
                Array(gridItems.enumerated()),
                id: \.offset
            ) { _, item in
                BentoCard(
                    icon: item.icon,
                    title: item.title,
                    subtitle: item.subtitle
                )
                .accessibilityIdentifier(
                    "\(tags.EXPLORE_ITEM)-\(item.id)"
                )
                .onTapGesture {
                    state.eventSink(
                        HomeEvent.ExploreItemClicked(id: item.id)
                    )
                }
            }
        }
    }

    private var exploreListItems: some View {
        let gridCount = isLandscape ? 3 : 2
        return ForEach(
            Array(state.exploreItems.dropFirst(gridCount).enumerated()),
            id: \.offset
        ) { _, item in
            HStack(spacing: MockDimens.spacingLg) {
                Circle()
                    .fill(colors.surfaceContainerHighest)
                    .frame(width: MockDimens.iconLg, height: MockDimens.iconLg)
                    .overlay(Text(item.icon))
                VStack(alignment: .leading) {
                    Text(item.title)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                    Text(item.subtitle)
                        .font(.caption)
                        .foregroundColor(
                            colors.onSurface.opacity(0.5)
                        )
                }
                Spacer()
                Text(">")
                    .foregroundColor(
                        colors.onSurface.opacity(0.3)
                    )
            }
            .padding(MockDimens.spacingXl)
            .background(colors.surfaceContainerLow)
            .cornerRadius(MockDimens.radiusMd)
            .accessibilityIdentifier(
                "\(tags.EXPLORE_ITEM)-\(item.id)"
            )
            .onTapGesture {
                state.eventSink(
                    HomeEvent.ExploreItemClicked(id: item.id)
                )
            }
        }
    }
}

struct CravingCard: View {
    @Environment(\.mockDonaldsColors) private var colors
    let title: String
    let subtitle: String
    let imageUrl: String
    var onTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            AsyncImage(
                url: URL(string: imageUrl),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fill)
                },
                placeholder: {
                    colors.surfaceContainerHigh
                }
            )
            .frame(width: MockDimens.cardWidth, height: MockDimens.cardHeight)
            .clipped()

            HStack {
                VStack(alignment: .leading) {
                    Text(title)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(
                            colors.onSurface.opacity(0.5)
                        )
                }
                Spacer()
                Circle()
                    .fill(colors.surfaceContainerHighest)
                    .frame(width: MockDimens.iconMd, height: MockDimens.iconMd)
                    .overlay(
                        Text("+")
                            .fontWeight(.bold)
                            .foregroundColor(colors.secondary)
                    )
            }
            .padding(20)
        }
        .frame(width: MockDimens.cardWidth)
        .background(colors.surfaceContainerLow)
        .cornerRadius(MockDimens.radiusMd)
        .onTapGesture(perform: onTap)
    }
}

struct BentoCard: View {
    @Environment(\.mockDonaldsColors) private var colors
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading) {
            Text(icon)
                .font(.title2)
                .foregroundColor(colors.secondary)
            Spacer()
            VStack(alignment: .leading) {
                Text(title)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onSurface)
                Text(subtitle)
                    .font(.caption2)
                    .foregroundColor(
                        colors.onSurface.opacity(0.5)
                    )
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: MockDimens.thumbnailHeight)
        .padding(MockDimens.spacingXl)
        .background(colors.surfaceContainerLow)
        .cornerRadius(MockDimens.radiusMd)
    }
}
