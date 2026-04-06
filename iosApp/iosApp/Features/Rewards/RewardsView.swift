import SwiftUI
import ComposeApp

struct RewardsView: View {
    let state: RewardsUiState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 48) {
                // Points Hero
                VStack(alignment: .leading) {
                    Text("CURRENT BALANCE")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .tracking(2)
                        .foregroundColor(MockDonaldsColors.secondary)
                        .padding(.bottom, 8)

                    HStack(alignment: .bottom, spacing: 8) {
                        Text("5,432")
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
                            Text("NEXT REWARD: GOLDEN BURGER")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                            Spacer()
                            Text("568 PTS TO GO")
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
                                    .frame(width: geo.size.width * 0.88, height: 12)
                            }
                        }
                        .frame(height: 12)
                    }
                    .padding(.top, 32)
                }

                // Vault Specials
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
                    }

                    // Large Feature
                    Color.clear
                        .frame(height: 256)
                        .overlay {
                            AsyncImage(url: URL(string: "https://lh3.googleusercontent.com/aida-public/AB6AXuB5_cdcHUFE84dPqS6Myqe6DPjZLm7pZA-e1xL-BJHKW6FCi5icL1OaYz6O0QLr7dMgVSBGZTVSR3DW_x8R6vqU-1yGdcX4FitIvyYNz2CpwgdZY3RzxncTcPO2LXm58UMBTeT3MfGELg7SGehbrXvkUKdOhMUnPoHl4z5gxJMOzk8axC97CfHSaJWx-eSv0ZrGXjxJslTIoNQTYmcHWAYA7aknA-NTcH69D36Q3_7mthLoelcYqIPuYCloGsEcM3-a-aehWSYx23g")) { image in
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
                                Text("EXCLUSIVE")
                                    .font(.caption2)
                                    .fontWeight(.bold)
                                    .tracking(1)
                                    .foregroundColor(Color(hex: 0x3F2E00))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 4)
                                    .background(MockDonaldsColors.secondary)
                                    .clipShape(Capsule())

                                Text("The Midnight Wagyu")
                                    .font(.title2)
                                    .fontWeight(.black)
                                    .foregroundColor(MockDonaldsColors.onSurface)
                                    .padding(.top, 12)
                                Text("2,500 PTS")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(MockDonaldsColors.secondary)
                                    .padding(.top, 4)
                            }
                            .padding(24)
                        }
                    .cornerRadius(12)

                    // Secondary Specials
                    HStack(spacing: 16) {
                        VaultSpecialCard(
                            title: "Truffle Penne",
                            points: "1,200 PTS",
                            imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuAtHYBNce4Wzgh78FbEg0YNoKbsdq-hHJUw2wrcV-wEYo3SNwtvHYwoqnnnOIGdLp43aAMfr8hYCP8COxfQVNjzEr9KOR0efa8_4WR8xQE-5h0zGVsaG0tc0NPiahRIFU5FXttF6_u6UrOdHCnmJgOhjeyNsgmLOv0rclMNNkWmxsfgLjH2UpmjWyzuir5SJ4y5uGhKA0Ffw4iBaWJqDdEvJVixU4liT-OqUP7F6dSYbT7IYKonLHCnzcDKGo0AZCKSsovifdDnlM"
                        )
                        VaultSpecialCard(
                            title: "Lava Souffle",
                            points: "850 PTS",
                            imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuCGwqtRYRBh2rJ9gv9F5Aj-NJIU0LV1aTQCuE-rAG-hc0Sp4HxZe68TmrfldrKtSWAyNhHps0VArNVduvzRYn7iju2ZUzmC0Ld1HgKNHCSgjcSPI6EiYYhlrRhTJiz9Lk5wmSFvZ9vjwTG6l6YLqr16HFuz9DHEoW5swuJDQYUGVMkxW-W8T_aJiKr8iM42PRBgnhBxVioMyJmqyIeZG0j4BgGaCLyK-v6mgNzlU5KAQmWDzhF4Vfr0JwBE4kOFH03cgiMyLgj8cLY"
                        )
                    }
                }

                // Earning History
                VStack(alignment: .leading, spacing: 16) {
                    Text("Earning History")
                        .font(.title3)
                        .fontWeight(.black)
                        .foregroundColor(MockDonaldsColors.onSurface)
                        .padding(.bottom, 8)

                    HistoryItemView(title: "Late Night Diner Order", subtitle: "Oct 24 \u{2022} Order #8821", points: "+125", isPositive: true, icon: "🍽️")
                    HistoryItemView(title: "The Midnight Wagyu", subtitle: "Oct 21 \u{2022} Reward Redeemed", points: "-2,500", isPositive: false, icon: "🎁")
                    HistoryItemView(title: "Birthday Bonus", subtitle: "Oct 18 \u{2022} Annual Gift", points: "+500", isPositive: true, icon: "🎉")
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
