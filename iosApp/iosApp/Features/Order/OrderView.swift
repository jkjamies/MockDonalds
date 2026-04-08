import SwiftUI
import ComposeApp

private let tags = OrderTestTags.shared

struct OrderView: View {
    let state: OrderUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 0) {
                    // Category Chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(Array(state.categories.enumerated()), id: \.offset) { _, category in
                                let isSelected = category.id == state.selectedCategoryId
                                Text(category.name)
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(isSelected ? Color(hex: 0xFFEBE8) : MockDonaldsColors.onSurface)
                                    .padding(.horizontal, 24)
                                    .padding(.vertical, 8)
                                    .background(isSelected ? MockDonaldsColors.primary : MockDonaldsColors.surfaceContainerHigh)
                                    .clipShape(Capsule())
                                    .accessibilityIdentifier("\(tags.CATEGORY_CHIP)-\(category.id)")
                                    .onTapGesture { state.eventSink(OrderEvent.CategorySelected(id: category.id)) }
                            }
                        }
                        .padding(.horizontal, 24)
                    }
                    .padding(.bottom, 32)

                    // Featured Items
                    VStack(spacing: 48) {
                        ForEach(Array(state.featuredItems.enumerated()), id: \.offset) { _, item in
                            FeaturedItemView(
                                title: item.title,
                                price: item.price,
                                description: item.description_,
                                imageUrl: item.imageUrl,
                                tag: item.tag,
                                isPrimary: item.isPrimary,
                                onAddToOrder: { state.eventSink(OrderEvent.AddToOrder(itemId: item.id)) }
                            )
                            .accessibilityIdentifier("\(tags.FEATURED_ITEM_CARD)-\(item.id)")
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 128)
                    .accessibilityIdentifier(tags.FEATURED_ITEMS_SECTION)
                }
            }

            // Floating Cart Bar
            if let cart = state.cartSummary {
                HStack {
                    HStack(spacing: 12) {
                        Circle()
                            .fill(Color(hex: 0x690001).opacity(0.2))
                            .frame(width: 32, height: 32)
                            .overlay(
                                Text("\(cart.itemCount)")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: 0xFFEBE8))
                            )
                        Text("\(cart.itemCount) ITEMS")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(Color(hex: 0xFFEBE8))
                            .tracking(2)
                    }
                    Spacer()
                    HStack(spacing: 8) {
                        Text(cart.total)
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
                .accessibilityIdentifier(tags.CART_BAR)
                .onTapGesture { state.eventSink(OrderEvent.CartClicked()) }
            }
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
    var onAddToOrder: () -> Void = {}

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

            Button(action: onAddToOrder) {
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
