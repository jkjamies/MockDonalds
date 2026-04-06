import SwiftUI
import ComposeApp

struct HomeView: View {
    let state: HomeUiState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 48) {
                // Greeting Section
                VStack(alignment: .leading, spacing: 4) {
                    Text("GOOD EVENING, GOURMET")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                    Text("Alex Mercer")
                        .font(.largeTitle)
                        .fontWeight(.black)
                        .foregroundColor(MockDonaldsColors.onSurface)
                }
                .padding(.horizontal, 24)

                // Hero Promotional Banner
                Color.clear
                    .frame(height: 480)
                    .overlay {
                        AsyncImage(url: URL(string: "https://lh3.googleusercontent.com/aida-public/AB6AXuD3XLmAyDLCbsMni75FhYeTO5jDtRg2NYPJKTxNbg4aleYFXC5xEXR3Y5hbq8-q9a5FKcQX6SBsY9mW5l3LFaBdXetovbtu6rFyBGMpAtYcCGVhtAhSsLhMOA9SzEQq6UUAVNhJMkSlDOtmR_L3LrJaSc-HN2aOIu7iEpAwKNrEWGP6Dk9E0tzIWrUEUQZCkrXKLjo2RtdB-aI_oRSLdnTwbnQPdXM5KV-VPld0gAdzjzoJrlCTM-h3Q5dEoLNSC587K_0ossB5VKQ")) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            MockDonaldsColors.surfaceContainerHigh
                        }
                    }
                    .clipped()
                    .overlay {
                        // Gradient overlays
                        LinearGradient(
                            colors: [.clear, MockDonaldsColors.background.opacity(0.9)],
                            startPoint: .top, endPoint: .bottom
                        )
                    }
                    .overlay {
                        LinearGradient(
                            colors: [MockDonaldsColors.background.opacity(0.6), .clear],
                            startPoint: .leading, endPoint: .trailing
                        )
                    }
                    .overlay(alignment: .bottomLeading) {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("LIMITED EDITION")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(Color(hex: 0x584200))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(MockDonaldsColors.secondary)
                                .clipShape(Capsule())

                            Text("Midnight Truffle Burger")
                                .font(.system(size: 36, weight: .black))
                                .foregroundColor(MockDonaldsColors.onSurface)
                                .lineSpacing(-4)

                            Text("Double-aged Wagyu beef, shaved Perigord truffles, and gold-dusted brioche.")
                                .font(.subheadline)
                                .foregroundColor(MockDonaldsColors.onSurface.opacity(0.7))

                            Button(action: {}) {
                                Text("Experience Now")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: 0xFFEBE8))
                                    .padding(.horizontal, 32)
                                    .padding(.vertical, 16)
                                    .background(
                                        LinearGradient(
                                            colors: [MockDonaldsColors.primary, Color(hex: 0x930003)],
                                            startPoint: .leading, endPoint: .trailing
                                        )
                                    )
                                    .cornerRadius(6)
                            }
                            .padding(.top, 16)
                        }
                        .padding(32)
                    }

                // Recent Cravings Section
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
                            CravingCard(
                                title: "Smoky Bleu Royale",
                                subtitle: "$18.50 \u{2022} 2 days ago",
                                imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuAEUcSbBQQ1D_rTaoTt2M29Lrvc1k31ru-Yy2v9jETAX6Imb1Z3xkCnOGzwODlYEAmG-R6fG5GntSwCtMAyPKuKob4H0uGzqPSVCOkfE7akZl2l1MV_VPmX-9ixfI5RwMKqW6N2IX27gFWM7CErxP79myBoFx-sbk1M8I7I1lQ0aDnB0ung7sBer2nkJH2VaCbA4dlpnq0vXETYCe1I0rCRBosLC-VKu0aQONfSgfkE_LcYwSpNIIExgurOMxr6uB4R8nQlTeS622s"
                            )
                            CravingCard(
                                title: "Roseway Fries (L)",
                                subtitle: "$6.20 \u{2022} 5 days ago",
                                imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuAek0b7xlNHx9hvtl19bOmndTLwRB59O51tbuaU6ddCqFCFHI6dWkI_9_1NS1MSfhg9-VPJGH9Ete-6g-o1qPhb8Q1HKjd_LA2w_Si34P5v2NyiJwlP3SH-zp3fXQwvSZQHuv_VWKgBEqo1pHgqATbotVPqPv876wMCIDmrlT8U6vNb3MgskQ1Jg2mBjPu2mecMMi697HR-20rVDQQ4aFpxkIUTJwQg1WFVa9moh-LRTpsH-jKrtb9PiyIGEXeTtCLGJkrY8WQN5sM"
                            )
                            CravingCard(
                                title: "Velvet Cola Fizz",
                                subtitle: "$4.50 \u{2022} 1 week ago",
                                imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuDIID3SWN4ATvLcZ6sUpRRaN-NTdxu7MHXohCtFpcxQLSR5DjiSX14VgesUoV5lcNFJ-HGoLoEUkQDJNu7_WH9ftDn_N7OR63_ryAaOtRfIqegQ2VSeWkPQNfJuQtsFdElJH3gLhP-m-fGpl_mkNAzbEO-gfRSGjUxEWVGbNyOmn1OkstqlIRZinzg0cBA_jBBo7O7nGbos-OFvb1m6yeAJwLr53Yw32vXo62Tnkzxe8jg5wVEeBzEWFIxwZqt5MDwlm7HEG8_4Ln4"
                            )
                        }
                        .padding(.horizontal, 24)
                    }
                }

                // Explore Bento Grid
                VStack(alignment: .leading, spacing: 24) {
                    Text("Explore")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)

                    HStack(spacing: 16) {
                        BentoCard(icon: "O", title: "Find", subtitle: "A Lounge Near You")
                        BentoCard(icon: "\u{1F381}", title: "Gift Cards", subtitle: "Share the Flavor")
                    }

                    HStack(spacing: 16) {
                        Circle()
                            .fill(MockDonaldsColors.surfaceContainerHighest)
                            .frame(width: 48, height: 48)
                            .overlay(Text("\u{1F3A7}"))
                        VStack(alignment: .leading) {
                            Text("Concierge Support")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(MockDonaldsColors.onSurface)
                            Text("Direct line to our kitchen masters")
                                .font(.caption)
                                .foregroundColor(MockDonaldsColors.onSurface.opacity(0.5))
                        }
                        Spacer()
                        Text(">")
                            .foregroundColor(MockDonaldsColors.onSurface.opacity(0.3))
                    }
                    .padding(24)
                    .background(MockDonaldsColors.surfaceContainerLow)
                    .cornerRadius(12)
                }
                .padding(.horizontal, 24)

                Spacer().frame(height: 24)
            }
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }
}

struct CravingCard: View {
    let title: String
    let subtitle: String
    let imageUrl: String

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            AsyncImage(url: URL(string: imageUrl)) { image in
                image.resizable().aspectRatio(contentMode: .fill)
            } placeholder: {
                MockDonaldsColors.surfaceContainerHigh
            }
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
                        .foregroundColor(MockDonaldsColors.onSurface.opacity(0.5))
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
                    .foregroundColor(MockDonaldsColors.onSurface.opacity(0.5))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 160)
        .padding(24)
        .background(MockDonaldsColors.surfaceContainerLow)
        .cornerRadius(12)
    }
}
