import SwiftUI
import ComposeApp

private let tags = HomeTestTags.shared

struct HomeView: View {
    let state: HomeUiState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 48) {
                greetingSection
                heroBanner
                recentCravingsSection
                exploreSection
                Spacer().frame(height: 24)
            }
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }

    private var greetingSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("GOOD EVENING, GOURMET")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(MockDonaldsColors.onSurfaceVariant)
            Text(state.userName)
                .font(.largeTitle)
                .fontWeight(.black)
                .foregroundColor(MockDonaldsColors.onSurface)
                .accessibilityIdentifier(tags.USER_NAME)
        }
        .padding(.horizontal, 24)
    }

    @ViewBuilder
    private var heroBanner: some View {
        if let hero = state.heroPromotion {
            Color.clear
                .frame(height: 480)
                .overlay {
                    AsyncImage(
                        url: URL(string: hero.imageUrl),
                        content: { image in
                            image.resizable()
                                .aspectRatio(contentMode: .fill)
                        },
                        placeholder: {
                            MockDonaldsColors.surfaceContainerHigh
                        }
                    )
                }
                .clipped()
                .overlay {
                    LinearGradient(
                        colors: [
                            .clear,
                            MockDonaldsColors.background.opacity(0.9),
                        ],
                        startPoint: .top, endPoint: .bottom
                    )
                }
                .overlay {
                    LinearGradient(
                        colors: [
                            MockDonaldsColors.background.opacity(0.6),
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
        VStack(alignment: .leading, spacing: 16) {
            Text(hero.tag)
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(Color(hex: 0x584200))
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background(MockDonaldsColors.secondary)
                .clipShape(Capsule())

            Text(hero.title)
                .font(.system(size: 36, weight: .black))
                .foregroundColor(MockDonaldsColors.onSurface)
                .lineSpacing(-4)

            Text(hero.description_)
                .font(.subheadline)
                .foregroundColor(
                    MockDonaldsColors.onSurface.opacity(0.7)
                )

            Button(
                action: {
                    state.eventSink(HomeEvent.HeroCtaClicked())
                },
                label: {
                    Text(hero.ctaText)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: 0xFFEBE8))
                    .padding(.horizontal, 32)
                    .padding(.vertical, 16)
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
            .accessibilityIdentifier(tags.HERO_CTA_BUTTON)
            .padding(.top, 16)
        }
        .padding(32)
    }

    @ViewBuilder
    private var recentCravingsSection: some View {
        if !state.recentCravings.isEmpty {
            VStack(alignment: .leading, spacing: 24) {
                HStack {
                    Text("Recent Cravings")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                    Spacer()
                    Text("View All")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.secondary)
                }
                .padding(.horizontal, 24)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 24) {
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
                    .padding(.horizontal, 24)
                }
            }
            .accessibilityIdentifier(tags.RECENT_CRAVINGS_SECTION)
        }
    }

    @ViewBuilder
    private var exploreSection: some View {
        if !state.exploreItems.isEmpty {
            VStack(alignment: .leading, spacing: 24) {
                Text("Explore")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.onSurface)

                exploreBentoGrid
                exploreListItems
            }
            .padding(.horizontal, 24)
            .accessibilityIdentifier(tags.EXPLORE_SECTION)
        }
    }

    private var exploreBentoGrid: some View {
        let gridItems = Array(state.exploreItems.prefix(2))
        return HStack(spacing: 16) {
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
        ForEach(
            Array(state.exploreItems.dropFirst(2).enumerated()),
            id: \.offset
        ) { _, item in
            HStack(spacing: 16) {
                Circle()
                    .fill(MockDonaldsColors.surfaceContainerHighest)
                    .frame(width: 48, height: 48)
                    .overlay(Text(item.icon))
                VStack(alignment: .leading) {
                    Text(item.title)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                    Text(item.subtitle)
                        .font(.caption)
                        .foregroundColor(
                            MockDonaldsColors.onSurface.opacity(0.5)
                        )
                }
                Spacer()
                Text(">")
                    .foregroundColor(
                        MockDonaldsColors.onSurface.opacity(0.3)
                    )
            }
            .padding(24)
            .background(MockDonaldsColors.surfaceContainerLow)
            .cornerRadius(12)
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
                    MockDonaldsColors.surfaceContainerHigh
                }
            )
            .frame(width: 288, height: 176)
            .clipped()

            HStack {
                VStack(alignment: .leading) {
                    Text(title)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(
                            MockDonaldsColors.onSurface.opacity(0.5)
                        )
                }
                Spacer()
                Circle()
                    .fill(MockDonaldsColors.surfaceContainerHighest)
                    .frame(width: 40, height: 40)
                    .overlay(
                        Text("+")
                            .fontWeight(.bold)
                            .foregroundColor(MockDonaldsColors.secondary)
                    )
            }
            .padding(20)
        }
        .frame(width: 288)
        .background(MockDonaldsColors.surfaceContainerLow)
        .cornerRadius(12)
        .onTapGesture(perform: onTap)
    }
}

struct BentoCard: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading) {
            Text(icon)
                .font(.title2)
                .foregroundColor(MockDonaldsColors.secondary)
            Spacer()
            VStack(alignment: .leading) {
                Text(title)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.onSurface)
                Text(subtitle)
                    .font(.caption2)
                    .foregroundColor(
                        MockDonaldsColors.onSurface.opacity(0.5)
                    )
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 160)
        .padding(24)
        .background(MockDonaldsColors.surfaceContainerLow)
        .cornerRadius(12)
    }
}
