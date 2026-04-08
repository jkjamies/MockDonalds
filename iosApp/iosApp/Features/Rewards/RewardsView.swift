import SwiftUI
import ComposeApp

private let tags = RewardsTestTags.shared

struct RewardsView: View {
    let state: RewardsUiState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 48) {
                // Points Hero
                if let progress = state.progress {
                    VStack(alignment: .leading) {
                        Text("CURRENT BALANCE")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .tracking(2)
                            .foregroundColor(MockDonaldsColors.secondary)
                            .padding(.bottom, 8)

                        HStack(alignment: .bottom, spacing: 8) {
                            Text(NumberFormatter.localizedString(from: NSNumber(value: progress.currentPoints), number: .decimal))
                                .font(.system(size: 64, weight: .black))
                                .foregroundColor(MockDonaldsColors.onSurface)
                            Text("PTS")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(Color(hex: 0xFFDF99))
                                .padding(.bottom, 8)
                        }

                        // Tier Progress
                        VStack(spacing: 16) {
                            HStack {
                                Text("NEXT REWARD: \(progress.nextRewardName.uppercased())")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                                Spacer()
                                Text("\(progress.pointsToNextReward) PTS TO GO")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(MockDonaldsColors.secondary)
                            }

                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    Capsule()
                                        .fill(MockDonaldsColors.surfaceContainerHighest)
                                        .frame(height: 12)
                                    Capsule()
                                        .fill(
                                            LinearGradient(
                                                colors: [MockDonaldsColors.primary, MockDonaldsColors.secondary],
                                                startPoint: .leading, endPoint: .trailing
                                            )
                                        )
                                        .frame(width: geo.size.width * CGFloat(progress.progressFraction), height: 12)
                                }
                            }
                            .frame(height: 12)
                        }
                        .padding(.top, 32)
                    }
                    .accessibilityIdentifier(tags.POINTS_SECTION)
                }

                // Vault Specials
                if !state.vaultSpecials.isEmpty {
                    VStack(alignment: .leading, spacing: 24) {
                        HStack {
                            Text("The Vault Specials")
                                .font(.title3)
                                .fontWeight(.black)
                                .foregroundColor(MockDonaldsColors.onSurface)
                            Spacer()
                            Text("VIEW ALL")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(MockDonaldsColors.secondary)
                                .accessibilityIdentifier(tags.VIEW_ALL)
                                .onTapGesture { state.eventSink(RewardsEvent.ViewAllClicked()) }
                        }

                        // Large Feature
                        if let featured = state.vaultSpecials.first(where: { $0.isFeatured }) {
                            Color.clear
                                .frame(height: 256)
                                .overlay {
                                    AsyncImage(url: URL(string: featured.imageUrl)) { image in
                                        image.resizable().aspectRatio(contentMode: .fill)
                                    } placeholder: {
                                        MockDonaldsColors.surfaceContainerHigh
                                    }
                                }
                                .clipped()
                                .overlay {
                                    LinearGradient(
                                        colors: [.clear, MockDonaldsColors.background.opacity(0.9)],
                                        startPoint: .top, endPoint: .bottom
                                    )
                                }
                                .overlay(alignment: .bottomLeading) {
                                    VStack(alignment: .leading, spacing: 0) {
                                        if let tag = featured.tag {
                                            Text(tag)
                                                .font(.caption2)
                                                .fontWeight(.bold)
                                                .tracking(1)
                                                .foregroundColor(Color(hex: 0x3F2E00))
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 4)
                                                .background(MockDonaldsColors.secondary)
                                                .clipShape(Capsule())
                                        }

                                        Text(featured.title)
                                            .font(.title2)
                                            .fontWeight(.black)
                                            .foregroundColor(MockDonaldsColors.onSurface)
                                            .padding(.top, 12)
                                        Text(featured.pointsCost)
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundColor(MockDonaldsColors.secondary)
                                            .padding(.top, 4)
                                    }
                                    .padding(24)
                                }
                                .cornerRadius(12)
                                .accessibilityIdentifier("\(tags.FEATURED_VAULT_CARD)-\(featured.id)")
                                .onTapGesture { state.eventSink(RewardsEvent.VaultSpecialClicked(id: featured.id)) }
                        }

                        // Secondary Specials
                        let secondary = state.vaultSpecials.filter { !$0.isFeatured }
                        if !secondary.isEmpty {
                            HStack(spacing: 16) {
                                ForEach(Array(secondary.enumerated()), id: \.offset) { _, special in
                                    VaultSpecialCard(
                                        title: special.title,
                                        points: special.pointsCost,
                                        imageUrl: special.imageUrl
                                    )
                                    .accessibilityIdentifier("\(tags.VAULT_SPECIAL_CARD)-\(special.id)")
                                    .onTapGesture { state.eventSink(RewardsEvent.VaultSpecialClicked(id: special.id)) }
                                }
                            }
                        }
                    }
                    .accessibilityIdentifier(tags.VAULT_SPECIALS_SECTION)
                }

                // Earning History
                if !state.history.isEmpty {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Earning History")
                            .font(.title3)
                            .fontWeight(.black)
                            .foregroundColor(MockDonaldsColors.onSurface)
                            .padding(.bottom, 8)

                        ForEach(Array(state.history.enumerated()), id: \.offset) { _, entry in
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
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }
}

struct VaultSpecialCard: View {
    let title: String
    let points: String
    let imageUrl: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            AsyncImage(url: URL(string: imageUrl)) { image in
                image.resizable().aspectRatio(contentMode: .fill)
            } placeholder: {
                MockDonaldsColors.surfaceContainerHighest
            }
            .aspectRatio(1, contentMode: .fill)
            .clipped()
            .cornerRadius(8)

            VStack(alignment: .leading) {
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.onSurface)
                Text(points)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.secondary)
            }
        }
        .padding(16)
        .background(MockDonaldsColors.surfaceContainerLow)
        .cornerRadius(12)
    }
}

struct HistoryItemView: View {
    let title: String
    let subtitle: String
    let points: String
    let isPositive: Bool
    let icon: String

    var body: some View {
        HStack {
            HStack(spacing: 16) {
                Circle()
                    .fill(MockDonaldsColors.surfaceContainerHighest)
                    .frame(width: 48, height: 48)
                    .overlay(Text(icon))
                VStack(alignment: .leading) {
                    Text(title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                    Text(subtitle)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                }
            }
            Spacer()
            Text(points)
                .font(.title3)
                .fontWeight(.black)
                .foregroundColor(isPositive ? MockDonaldsColors.secondary : MockDonaldsColors.primary)
        }
        .padding(20)
        .background(MockDonaldsColors.surface)
        .cornerRadius(12)
    }
}
