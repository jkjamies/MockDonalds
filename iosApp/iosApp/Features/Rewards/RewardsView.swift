import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = RewardsTestTags.shared

@CircuitInject(RewardsScreen.self, RewardsUiState.self)
struct RewardsView: View {
    let state: RewardsUiState
    @Environment(\.samplePlatterColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXxxl) {
                if isLandscape {
                    // Two-column: points hero left (~40%), vault specials right (~60%)
                    HStack(alignment: .top, spacing: PlatterDimens.spacingXxl) {
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
            .padding(.horizontal, PlatterDimens.spacingXl)
            .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
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
                    .padding(.bottom, PlatterDimens.spacingSm)

                HStack(alignment: .bottom, spacing: PlatterDimens.spacingSm) {
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
                        .padding(.bottom, PlatterDimens.spacingSm)
                }

                tierProgress(progress: progress)
            }
            .accessibilityIdentifier(tags.POINTS_SECTION)
        }
    }

    private func tierProgress(
        progress: RewardsProgress
    ) -> some View {
        VStack(spacing: PlatterDimens.spacingLg) {
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
                        .frame(height: PlatterDimens.spacingMd)
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
                            height: PlatterDimens.spacingMd
                        )
                }
            }
            .frame(height: PlatterDimens.spacingMd)
        }
        .padding(.top, PlatterDimens.spacingXxl)
    }

    @ViewBuilder
    private var vaultSpecialsSection: some View {
        if !state.vaultSpecials.isEmpty {
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXl) {
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
                .cornerRadius(PlatterDimens.radiusMd)
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
                    .padding(.horizontal, PlatterDimens.spacingMd)
                    .padding(.vertical, PlatterDimens.spacingXs)
                    .background(colors.secondary)
                    .clipShape(Capsule())
            }

            Text(featured.title)
                .font(.title2)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
                .padding(.top, PlatterDimens.spacingMd)
            Text(featured.pointsCost)
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(colors.secondary)
                .padding(.top, PlatterDimens.spacingXs)
        }
        .padding(PlatterDimens.spacingXl)
    }

    @ViewBuilder
    private var secondarySpecials: some View {
        let secondary = state.vaultSpecials.filter {
            !$0.isFeatured
        }
        if !secondary.isEmpty {
            HStack(spacing: PlatterDimens.spacingLg) {
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
            VStack(alignment: .leading, spacing: PlatterDimens.spacingLg) {
                Text("Earning History")
                    .font(.title3)
                    .fontWeight(.black)
                    .foregroundColor(colors.onSurface)
                    .padding(.bottom, PlatterDimens.spacingSm)

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
    @Environment(\.samplePlatterColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: PlatterDimens.spacingLg) {
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
            .cornerRadius(PlatterDimens.spacingSm)

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
        .padding(PlatterDimens.spacingLg)
        .background(colors.surfaceContainerLow)
        .cornerRadius(PlatterDimens.radiusMd)
    }
}

struct HistoryItemView: View {
    let title: String
    let subtitle: String
    let points: String
    let isPositive: Bool
    let icon: String
    @Environment(\.samplePlatterColors) private var colors

    var body: some View {
        HStack {
            HStack(spacing: PlatterDimens.spacingLg) {
                Circle()
                    .fill(colors.surfaceContainerHighest)
                    .frame(width: PlatterDimens.iconLg, height: PlatterDimens.iconLg)
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
        .cornerRadius(PlatterDimens.radiusMd)
    }
}
