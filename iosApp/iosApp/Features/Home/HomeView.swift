import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = HomeTestTags.shared

@CircuitInject(HomeScreen.self, HomeUiState.self)
struct HomeView: View {
    @Environment(\.samplePlatterColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    let state: HomeUiState

    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXxxl) {
                greetingSection
                heroBanner
                recentCravingsSection
                exploreSection
                Spacer().frame(height: PlatterDimens.spacingXl)
            }
            .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
        }
        .background(colors.background)
    }

    private var greetingSection: some View {
        VStack(alignment: .leading, spacing: PlatterDimens.spacingXs) {
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
        .padding(.horizontal, PlatterDimens.spacingXl)
    }

    @ViewBuilder
    private var heroBanner: some View {
        if let hero = state.heroPromotion {
            Color.clear
                .frame(height: PlatterDimens.adaptiveHeroHeight(isLandscape: isLandscape))
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
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier(tags.HERO_BANNER)
                .overlay(alignment: .bottomLeading) {
                    heroOverlayContent(hero: hero)
                }
        }
    }

    private func heroOverlayContent(
        hero: HeroPromotion
    ) -> some View {
        VStack(alignment: .leading, spacing: PlatterDimens.spacingLg) {
            Text(hero.tag)
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(colors.onSecondaryTag)
                .padding(.horizontal, PlatterDimens.spacingMd)
                .padding(.vertical, PlatterDimens.spacingXs)
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
                    .padding(.horizontal, PlatterDimens.spacingXxl)
                    .padding(.vertical, PlatterDimens.spacingLg)
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
            .accessibilityIdentifier(tags.HERO_CTA_BUTTON)
            .padding(.top, PlatterDimens.spacingLg)
        }
        .padding(PlatterDimens.spacingXxl)
    }

    @ViewBuilder
    private var recentCravingsSection: some View {
        if !state.recentCravings.isEmpty {
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXl) {
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
                .padding(.horizontal, PlatterDimens.spacingXl)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: PlatterDimens.spacingXl) {
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
                    .padding(.horizontal, PlatterDimens.spacingXl)
                }
            }
            .accessibilityIdentifier(tags.RECENT_CRAVINGS_SECTION)
        }
    }

    @ViewBuilder
    private var exploreSection: some View {
        if !state.exploreItems.isEmpty {
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXl) {
                Text("Explore")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onSurface)

                exploreBentoGrid
                exploreListItems
            }
            .padding(.horizontal, PlatterDimens.spacingXl)
            .accessibilityIdentifier(tags.EXPLORE_SECTION)
        }
    }

    private var exploreBentoGrid: some View {
        let gridCount = isLandscape ? 3 : 2
        let gridItems = Array(state.exploreItems.prefix(gridCount))
        return HStack(spacing: PlatterDimens.spacingLg) {
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
            HStack(spacing: PlatterDimens.spacingLg) {
                Circle()
                    .fill(colors.surfaceContainerHighest)
                    .frame(width: PlatterDimens.iconLg, height: PlatterDimens.iconLg)
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
            .padding(PlatterDimens.spacingXl)
            .background(colors.surfaceContainerLow)
            .cornerRadius(PlatterDimens.radiusMd)
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
    @Environment(\.samplePlatterColors) private var colors
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
            .frame(width: PlatterDimens.cardWidth, height: PlatterDimens.cardHeight)
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
                    .frame(width: PlatterDimens.iconMd, height: PlatterDimens.iconMd)
                    .overlay(
                        Text("+")
                            .fontWeight(.bold)
                            .foregroundColor(colors.secondary)
                    )
            }
            .padding(20)
        }
        .frame(width: PlatterDimens.cardWidth)
        .background(colors.surfaceContainerLow)
        .cornerRadius(PlatterDimens.radiusMd)
        .onTapGesture(perform: onTap)
    }
}

struct BentoCard: View {
    @Environment(\.samplePlatterColors) private var colors
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
        .frame(height: PlatterDimens.thumbnailHeight)
        .padding(PlatterDimens.spacingXl)
        .background(colors.surfaceContainerLow)
        .cornerRadius(PlatterDimens.radiusMd)
    }
}
