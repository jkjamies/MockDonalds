import SwiftUI
import ComposeApp

private let tags = RewardsTestTags.shared

struct RewardsView: View {
    let state: RewardsUiState
    @Environment(\.mockDonaldsColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: MockDimens.spacingXxxl) {
                if isLandscape {
                    // Two-column: points hero left (~40%), vault specials right (~60%)
                    HStack(alignment: .top, spacing: MockDimens.spacingXxl) {
                        pointsHeroSection
                            .frame(maxWidth: .infinity)
                        vaultSpecialsSection
                            .frame(maxWidth: .infinity)
                    }
                } else {
                    pointsHeroSection
                    vaultSpecialsSection
                }
                earningHistorySection
            }
            .padding(.horizontal, MockDimens.spacingXl)
            .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
        }
        .background(colors.background)
    }

    @ViewBuilder
    private var pointsHeroSection: some View {
        if let progress = state.progress {
            VStack(alignment: .leading) {
                Text("CURRENT BALANCE")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .tracking(2)
                    .foregroundColor(colors.secondary)
                    .padding(.bottom, MockDimens.spacingSm)

                HStack(alignment: .bottom, spacing: MockDimens.spacingSm) {
                    Text(
                        NumberFormatter.localizedString(
                            from: NSNumber(
                                value: progress.currentPoints
                            ),
                            number: .decimal
                        )
                    )
                    .font(.system(size: 64, weight: .black))
                    .foregroundColor(colors.onSurface)
                    Text("PTS")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(colors.secondaryLight)
                        .padding(.bottom, MockDimens.spacingSm)
                }

                tierProgress(progress: progress)
            }
            .accessibilityIdentifier(tags.POINTS_SECTION)
        }
    }

    private func tierProgress(
        progress: RewardsProgress
    ) -> some View {
        VStack(spacing: MockDimens.spacingLg) {
            HStack {
                Text(
                    "NEXT REWARD: "
                    + progress.nextRewardName.uppercased()
                )
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(
                    colors.onSurfaceVariant
                )
                Spacer()
                Text("\(progress.pointsToNextReward) PTS TO GO")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.secondary)
            }

            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(
                            colors
                                .surfaceContainerHighest
                        )
                        .frame(height: MockDimens.spacingMd)
                    Capsule()
                        .fill(
                            LinearGradient(
                                colors: [
                                    colors.primary,
                                    colors.secondary,
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(
                            width: geo.size.width
                                * CGFloat(progress.progressFraction),
                            height: MockDimens.spacingMd
                        )
                }
            }
            .frame(height: MockDimens.spacingMd)
        }
        .padding(.top, MockDimens.spacingXxl)
    }

    @ViewBuilder
    private var vaultSpecialsSection: some View {
        if !state.vaultSpecials.isEmpty {
            VStack(alignment: .leading, spacing: MockDimens.spacingXl) {
                vaultSpecialsHeader
                featuredVaultCard
                secondarySpecials
            }
            .accessibilityIdentifier(tags.VAULT_SPECIALS_SECTION)
        }
    }

    private var vaultSpecialsHeader: some View {
        HStack {
            Text("The Vault Specials")
                .font(.title3)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
            Spacer()
            Text("VIEW ALL")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(colors.secondary)
                .accessibilityIdentifier(tags.VIEW_ALL)
                .onTapGesture {
                    state.eventSink(
                        RewardsEvent.ViewAllClicked()
                    )
                }
        }
    }

    @ViewBuilder
    private var featuredVaultCard: some View {
        if let featured = state.vaultSpecials.first(
            where: { $0.isFeatured }
        ) {
            Color.clear
                .frame(height: 256)
                .overlay {
                    AsyncImage(
                        url: URL(string: featured.imageUrl),
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
                            colors.background
                                .opacity(0.9),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }
                .overlay(alignment: .bottomLeading) {
                    featuredOverlay(featured: featured)
                }
                .cornerRadius(MockDimens.radiusMd)
                .accessibilityIdentifier(
                    "\(tags.FEATURED_VAULT_CARD)-\(featured.id)"
                )
                .onTapGesture {
                    state.eventSink(
                        RewardsEvent.VaultSpecialClicked(
                            id: featured.id
                        )
                    )
                }
        }
    }

    private func featuredOverlay(
        featured: VaultSpecial
    ) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            if let tag = featured.tag {
                Text(tag)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .tracking(1)
                    .foregroundColor(colors.onSecondaryContainer)
                    .padding(.horizontal, MockDimens.spacingMd)
                    .padding(.vertical, MockDimens.spacingXs)
                    .background(colors.secondary)
                    .clipShape(Capsule())
            }

            Text(featured.title)
                .font(.title2)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
                .padding(.top, MockDimens.spacingMd)
            Text(featured.pointsCost)
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(colors.secondary)
                .padding(.top, MockDimens.spacingXs)
        }
        .padding(MockDimens.spacingXl)
    }

    @ViewBuilder
    private var secondarySpecials: some View {
        let secondary = state.vaultSpecials.filter {
            !$0.isFeatured
        }
        if !secondary.isEmpty {
            HStack(spacing: MockDimens.spacingLg) {
                ForEach(
                    Array(secondary.enumerated()),
                    id: \.offset
                ) { _, special in
                    VaultSpecialCard(
                        title: special.title,
                        points: special.pointsCost,
                        imageUrl: special.imageUrl
                    )
                    .accessibilityIdentifier(
                        "\(tags.VAULT_SPECIAL_CARD)-\(special.id)"
                    )
                    .onTapGesture {
                        state.eventSink(
                            RewardsEvent.VaultSpecialClicked(
                                id: special.id
                            )
                        )
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var earningHistorySection: some View {
        if !state.history.isEmpty {
            VStack(alignment: .leading, spacing: MockDimens.spacingLg) {
                Text("Earning History")
                    .font(.title3)
                    .fontWeight(.black)
                    .foregroundColor(colors.onSurface)
                    .padding(.bottom, MockDimens.spacingSm)

                ForEach(
                    Array(state.history.enumerated()),
                    id: \.offset
                ) { _, entry in
                    HistoryItemView(
                        title: entry.title,
                        subtitle: entry.subtitle,
                        points: entry.points,
                        isPositive: entry.isPositive,
                        icon: entry.icon
                    )
                }
            }
            .accessibilityIdentifier(tags.HISTORY_SECTION)
        }
    }
}

struct VaultSpecialCard: View {
    let title: String
    let points: String
    let imageUrl: String
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingLg) {
            AsyncImage(
                url: URL(string: imageUrl),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fill)
                },
                placeholder: {
                    colors.surfaceContainerHighest
                }
            )
            .aspectRatio(1, contentMode: .fill)
            .clipped()
            .cornerRadius(MockDimens.spacingSm)

            VStack(alignment: .leading) {
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onSurface)
                Text(points)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.secondary)
            }
        }
        .padding(MockDimens.spacingLg)
        .background(colors.surfaceContainerLow)
        .cornerRadius(MockDimens.radiusMd)
    }
}

struct HistoryItemView: View {
    let title: String
    let subtitle: String
    let points: String
    let isPositive: Bool
    let icon: String
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        HStack {
            HStack(spacing: MockDimens.spacingLg) {
                Circle()
                    .fill(colors.surfaceContainerHighest)
                    .frame(width: MockDimens.iconLg, height: MockDimens.iconLg)
                    .overlay(Text(icon))
                VStack(alignment: .leading) {
                    Text(title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                    Text(subtitle)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(
                            colors.onSurfaceVariant
                        )
                }
            }
            Spacer()
            Text(points)
                .font(.title3)
                .fontWeight(.black)
                .foregroundColor(
                    isPositive
                        ? colors.secondary
                        : colors.primary
                )
        }
        .padding(20)
        .background(colors.surface)
        .cornerRadius(MockDimens.radiusMd)
    }
}
