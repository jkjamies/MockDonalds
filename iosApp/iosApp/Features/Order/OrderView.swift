import SwiftUI
import ComposeApp

struct OrderView: View {
    let state: OrderUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 0) {
                    // Category Chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(["Burgers", "Fries", "Drinks", "Desserts"], id: \.self) { name in
                                let isSelected = name == "Burgers"
                                Text(name)
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(isSelected ? Color(hex: 0xFFEBE8) : MockDonaldsColors.onSurface)
                                    .padding(.horizontal, 24)
                                    .padding(.vertical, 8)
                                    .background(isSelected ? MockDonaldsColors.primary : MockDonaldsColors.surfaceContainerHigh)
                                    .clipShape(Capsule())
                            }
                        }
                        .padding(.horizontal, 24)
                    }
                    .padding(.bottom, 32)

                    // Featured Items
                    VStack(spacing: 48) {
                        FeaturedItemView(
                            title: "Midnight Truffle",
                            price: "$24",
                            description: "Double wagyu beef, black truffle aioli, aged gruyere, and caramelized balsamic onions on a charcoal brioche bun.",
                            imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuCIgoaLEiJ_bs2e8Me_lZ4aFmkrX6vIJZNq8pxZvTHgTWm1Tf3owVjnv0TB10EdFBml3pZnGq5zSKGEA7e-jieP5DA8Z6TPrl-bZubc97xrDi06vNYqb2tQQ4lyimnkB7D0ea0DQWBUuDI399M7wip6bz1Sx03HyAqp4FKPE8QDJezB45YFf3lOWquJQm0PordZaY7vResoMshyeZI6C2VR-oryDi51W3sAThDRaUZdHSPcZOxs_DxnsPssQmc8SzuMxeh3BbD_iDQ",
                            tag: "SIGNATURE",
                            isPrimary: true
                        )
                        FeaturedItemView(
                            title: "Saffron Fries",
                            price: "$12",
                            description: "Triple-cooked hand-cut batons dusted with Kashmiri saffron and served with a roasted garlic confit dip.",
                            imageUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuANtMk8nUlKAA2ReyocZY_KslUkl91nRJwy1_LJJXjCWfbl8XW6LpFS4Ho6KvqoTJEpVs7O0Bp0W7VBmi16AOTa73CdSIi4EjuqgG3X1_nE-JOy1KeQwEk1CpHrpPA5cz5u2JvkOQrhHnc8CGSwWgaSXmNn3bSXD0KdBca78UZzHk1p9PAWGuOfzJALFy8yKPj3JvcBz9CCMPcZgmTWVtipd8bRea_17N4VetRnVtPjz5Nx13eA2qweBqYtgQqzgRVnSrx-1r24gMM",
                            tag: "TRENDING",
                            isPrimary: false
                        )
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 128)
                }
            }

            // Floating Cart Bar
            HStack {
                HStack(spacing: 12) {
                    Circle()
                        .fill(Color(hex: 0x690001).opacity(0.2))
                        .frame(width: 32, height: 32)
                        .overlay(
                            Text("2")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(Color(hex: 0xFFEBE8))
                        )
                    Text("2 ITEMS")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: 0xFFEBE8))
                        .tracking(2)
                }
                Spacer()
                HStack(spacing: 8) {
                    Text("$36.00")
                        .font(.title3)
                        .fontWeight(.black)
                        .foregroundColor(Color(hex: 0xFFEBE8))
                    Text("->")
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: 0xFFEBE8))
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(MockDonaldsColors.primary)
            .cornerRadius(12)
            .padding(.horizontal, 24)
            .padding(.bottom, 88)
        }
        .background(MockDonaldsColors.background)
    }
}

struct FeaturedItemView: View {
    let title: String
    let price: String
    let description: String
    let imageUrl: String
    let tag: String
    let isPrimary: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack(alignment: isPrimary ? .topTrailing : .topLeading) {
                Color.clear
                    .aspectRatio(4.0 / 3.0, contentMode: .fit)
                    .overlay {
                        AsyncImage(url: URL(string: imageUrl)) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            MockDonaldsColors.surfaceContainerLow
                        }
                    }
                    .clipped()
                    .cornerRadius(12)

                Text(tag)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.secondary)
                    .tracking(1)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(MockDonaldsColors.secondary.opacity(0.2))
                    .clipShape(Capsule())
                    .padding(16)
            }

            HStack(alignment: .bottom) {
                Text(title)
                    .font(.system(size: 32, weight: .black))
                    .foregroundColor(MockDonaldsColors.onSurface)
                Spacer()
                Text(price)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.secondary)
            }

            Text(description)
                .font(.subheadline)
                .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                .padding(.bottom, 16)

            Button(action: {}) {
                HStack(spacing: 8) {
                    Text("+").fontWeight(.bold)
                    Text("ADD TO ORDER").font(.caption).fontWeight(.bold)
                }
                .foregroundColor(isPrimary ? Color(hex: 0xFFEBE8) : MockDonaldsColors.secondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                    isPrimary
                        ? AnyShapeStyle(LinearGradient(colors: [MockDonaldsColors.primary, Color(hex: 0x930003)], startPoint: .leading, endPoint: .trailing))
                        : AnyShapeStyle(MockDonaldsColors.surfaceContainerHighest)
                )
                .cornerRadius(6)
            }
        }
    }
}
