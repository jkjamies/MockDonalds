import SwiftUI
import ComposeApp

private let tags = OrderTestTags.shared

struct OrderView: View {
    @Environment(\.mockDonaldsColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    private var isLandscape: Bool { verticalSizeClass == .compact }

    let state: OrderUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 0) {
                    categoryChipsSection
                    featuredItemsSection
                }
            }

            cartBar
        }
        .background(colors.background)
    }

    private var categoryChipsSection: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: MockDimens.spacingMd) {
                ForEach(
                    Array(state.categories.enumerated()),
                    id: \.offset
                ) { _, category in
                    let isSelected = category.id == state.selectedCategoryId
                    Text(category.name)
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(
                            isSelected
                                ? colors.onPrimaryButton
                                : colors.onSurface
                        )
                        .padding(.horizontal, MockDimens.spacingXl)
                        .padding(.vertical, MockDimens.spacingSm)
                        .background(
                            isSelected
                                ? colors.primary
                                : colors.surfaceContainerHigh
                        )
                        .clipShape(Capsule())
                        .accessibilityIdentifier(
                            "\(tags.CATEGORY_CHIP)-\(category.id)"
                        )
                        .onTapGesture {
                            state.eventSink(
                                OrderEvent.CategorySelected(id: category.id)
                            )
                        }
                }
            }
            .padding(.horizontal, MockDimens.spacingXl)
        }
        .padding(.bottom, MockDimens.spacingXxl)
    }

    private var featuredItemsSection: some View {
        Group {
            if isLandscape {
                // 2-column grid in landscape
                let items = Array(state.featuredItems.enumerated())
                let pairs = stride(from: 0, to: items.count, by: 2).map {
                    Array(items[$0..<min($0 + 2, items.count)])
                }
                VStack(spacing: MockDimens.spacingXxl) {
                    ForEach(Array(pairs.enumerated()), id: \.offset) { _, pair in
                        HStack(alignment: .top, spacing: MockDimens.spacingLg) {
                            ForEach(pair, id: \.offset) { _, item in
                                FeaturedItemView(
                                    title: item.title,
                                    price: item.price,
                                    description: item.description_,
                                    imageUrl: item.imageUrl,
                                    tag: item.tag,
                                    isPrimary: item.isPrimary,
                                    onAddToOrder: {
                                        state.eventSink(
                                            OrderEvent.AddToOrder(itemId: item.id)
                                        )
                                    }
                                )
                                .accessibilityIdentifier(
                                    "\(tags.FEATURED_ITEM_CARD)-\(item.id)"
                                )
                            }
                            if pair.count == 1 {
                                Spacer().frame(maxWidth: .infinity)
                            }
                        }
                    }
                }
            } else {
                VStack(spacing: MockDimens.spacingXxxl) {
                    ForEach(
                        Array(state.featuredItems.enumerated()),
                        id: \.offset
                    ) { _, item in
                        FeaturedItemView(
                            title: item.title,
                            price: item.price,
                            description: item.description_,
                            imageUrl: item.imageUrl,
                            tag: item.tag,
                            isPrimary: item.isPrimary,
                            onAddToOrder: {
                                state.eventSink(
                                    OrderEvent.AddToOrder(itemId: item.id)
                                )
                            }
                        )
                        .accessibilityIdentifier(
                            "\(tags.FEATURED_ITEM_CARD)-\(item.id)"
                        )
                    }
                }
            }
        }
        .padding(.horizontal, MockDimens.spacingXl)
        .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
        .accessibilityIdentifier(tags.FEATURED_ITEMS_SECTION)
    }

    @ViewBuilder
    private var cartBar: some View {
        if let cart = state.cartSummary {
            HStack {
                HStack(spacing: MockDimens.spacingMd) {
                    Circle()
                        .fill(colors.primaryDarker.opacity(0.2))
                        .frame(width: MockDimens.spacingXxl, height: MockDimens.spacingXxl)
                        .overlay(
                            Text("\(cart.itemCount)")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(colors.onPrimaryButton)
                        )
                    Text("\(cart.itemCount) ITEMS")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onPrimaryButton)
                        .tracking(2)
                }
                Spacer()
                HStack(spacing: MockDimens.spacingSm) {
                    Text(cart.total)
                        .font(.title3)
                        .fontWeight(.black)
                        .foregroundColor(colors.onPrimaryButton)
                    Text("->")
                        .fontWeight(.bold)
                        .foregroundColor(colors.onPrimaryButton)
                }
            }
            .padding(.horizontal, MockDimens.spacingXl)
            .padding(.vertical, MockDimens.spacingLg)
            .background(colors.primary)
            .cornerRadius(MockDimens.radiusMd)
            .padding(.horizontal, MockDimens.spacingXl)
            .padding(.bottom, 88)
            .accessibilityIdentifier(tags.CART_BAR)
            .onTapGesture {
                state.eventSink(OrderEvent.CartClicked())
            }
        }
    }
}

struct FeaturedItemView: View {
    @Environment(\.mockDonaldsColors) private var colors

    let title: String
    let price: String
    let description: String
    let imageUrl: String
    let tag: String
    let isPrimary: Bool
    var onAddToOrder: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingLg) {
            imageSection
            titleRow
            descriptionText
            addToOrderButton
        }
    }

    private var imageSection: some View {
        ZStack(alignment: isPrimary ? .topTrailing : .topLeading) {
            Color.clear
                .aspectRatio(4.0 / 3.0, contentMode: .fit)
                .overlay {
                    AsyncImage(
                        url: URL(string: imageUrl),
                        content: { image in
                            image.resizable()
                                .aspectRatio(contentMode: .fill)
                        },
                        placeholder: {
                            colors.surfaceContainerLow
                        }
                    )
                }
                .clipped()
                .cornerRadius(MockDimens.radiusMd)

            Text(tag)
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(colors.secondary)
                .tracking(1)
                .padding(.horizontal, MockDimens.spacingMd)
                .padding(.vertical, MockDimens.spacingXs)
                .background(
                    colors.secondary.opacity(0.2)
                )
                .clipShape(Capsule())
                .padding(MockDimens.spacingLg)
        }
    }

    private var titleRow: some View {
        HStack(alignment: .bottom) {
            Text(title)
                .font(.system(size: 32, weight: .black))
                .foregroundColor(colors.onSurface)
            Spacer()
            Text(price)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(colors.secondary)
        }
    }

    private var descriptionText: some View {
        Text(description)
            .font(.subheadline)
            .foregroundColor(colors.onSurfaceVariant)
            .padding(.bottom, MockDimens.spacingLg)
    }

    private var addToOrderButton: some View {
        Button(action: onAddToOrder) {
            HStack(spacing: MockDimens.spacingSm) {
                Text("+").fontWeight(.bold)
                Text("ADD TO ORDER")
                    .font(.caption).fontWeight(.bold)
            }
            .foregroundColor(
                isPrimary
                    ? colors.onPrimaryButton
                    : colors.secondary
            )
            .frame(maxWidth: .infinity)
            .padding(.vertical, MockDimens.spacingLg)
            .background(
                isPrimary
                    ? AnyShapeStyle(
                        LinearGradient(
                            colors: [
                                colors.primary,
                                colors.primaryDark,
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    : AnyShapeStyle(
                        colors.surfaceContainerHighest
                    )
            )
            .cornerRadius(MockDimens.radiusSm)
        }
    }
}
