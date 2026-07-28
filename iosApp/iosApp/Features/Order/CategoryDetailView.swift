import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = CategoryDetailTestTags.shared

/// Vertical offset reserved for the floating cart bar so the LazyVStack content
/// doesn't disappear beneath it. Mirrors `PlatterDimens.CartBarOffset` on Android
/// (64.dp); iOS uses 96 because the cart bar component is taller in SwiftUI.
private let cartBarOffset: CGFloat = 96

@CircuitInject(CategoryDetailScreen.self, CategoryDetailUiState.self)
struct CategoryDetailView: View {
    @Environment(\.samplePlatterColors) private var colors

    let state: CategoryDetailUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                topBar
                ScrollView {
                    LazyVStack(spacing: PlatterDimens.spacingXxl) {
                        ForEach(state.items, id: \.id) { item in
                            MenuItemCardView(
                                item: item,
                                onAddToOrder: {
                                    state.eventSink(
                                        CategoryDetailEvent.AddToOrder(itemId: item.id)
                                    )
                                }
                            )
                            .accessibilityIdentifier(
                                "\(tags.MENU_ITEM_CARD)-\(item.id)"
                            )
                        }
                    }
                    .padding(.horizontal, PlatterDimens.spacingXl)
                    .padding(.top, PlatterDimens.spacingLg)
                    .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: false) + cartBarOffset)
                }
            }

            cartBar
        }
        .background(colors.background)
        .navigationBarBackButtonHidden(true)
    }

    private var topBar: some View {
        HStack {
            Button(
                action: { state.eventSink(CategoryDetailEvent.BackPressed()) },
                label: {
                    ZStack {
                        Circle()
                            .fill(colors.surfaceContainerHigh)
                            .frame(
                                width: PlatterDimens.spacingXxl,
                                height: PlatterDimens.spacingXxl
                            )
                        Text("<")
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundColor(colors.onSurface)
                    }
                }
            )
            .accessibilityIdentifier(tags.BACK_BUTTON)

            Text(state.categoryName)
                .font(.title2)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)
                .padding(.leading, PlatterDimens.spacingLg)

            Spacer()
        }
        .padding(PlatterDimens.spacingXl)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(tags.TOP_BAR)
    }

    @ViewBuilder
    private var cartBar: some View {
        if let cart = state.cartSummary {
            OrderCartBarView(
                cart: cart,
                onTap: { state.eventSink(CategoryDetailEvent.CartClicked()) }
            )
            .accessibilityIdentifier(tags.CART_BAR)
        }
    }
}

struct MenuItemCardView: View {
    @Environment(\.samplePlatterColors) private var colors

    let item: MenuItem
    var onAddToOrder: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: PlatterDimens.spacingMd) {
            Color.clear
                .aspectRatio(16.0 / 9.0, contentMode: .fit)
                .overlay {
                    AsyncImage(
                        url: URL(string: item.imageUrl),
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
                .cornerRadius(PlatterDimens.radiusMd)

            Text(item.title)
                .font(.title3)
                .fontWeight(.black)
                .foregroundColor(colors.onSurface)

            Text(item.restaurantChain)
                .font(.subheadline)
                .foregroundColor(colors.onSurfaceVariant)

            if let serving = item.servingSize {
                Text(serving)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
            }

            Button(action: onAddToOrder) {
                Text("+ ADD TO ORDER")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.secondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, PlatterDimens.spacingLg)
                    .background(colors.surfaceContainerHighest)
                    .cornerRadius(PlatterDimens.radiusSm)
            }
            .accessibilityIdentifier("\(CategoryDetailTestTags.shared.ADD_TO_ORDER_BUTTON)-\(item.id)")
        }
    }
}
