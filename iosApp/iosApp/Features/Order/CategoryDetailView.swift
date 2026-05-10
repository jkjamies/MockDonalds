import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = CategoryDetailTestTags.shared

@CircuitInject(CategoryDetailScreen.self, CategoryDetailUiState.self)
struct CategoryDetailView: View {
    @Environment(\.mockDonaldsColors) private var colors

    let state: CategoryDetailUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                topBar
                ScrollView {
                    LazyVStack(spacing: MockDimens.spacingXxl) {
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
                    .padding(.horizontal, MockDimens.spacingXl)
                    .padding(.top, MockDimens.spacingLg)
                    .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: false) + 96)
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
                                width: MockDimens.spacingXxl,
                                height: MockDimens.spacingXxl
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
                .padding(.leading, MockDimens.spacingLg)

            Spacer()
        }
        .padding(MockDimens.spacingXl)
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
    @Environment(\.mockDonaldsColors) private var colors

    let item: MenuItem
    var onAddToOrder: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: MockDimens.spacingMd) {
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
                .cornerRadius(MockDimens.radiusMd)

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
                    .padding(.vertical, MockDimens.spacingLg)
                    .background(colors.surfaceContainerHighest)
                    .cornerRadius(MockDimens.radiusSm)
            }
            .accessibilityIdentifier("\(CategoryDetailTestTags.shared.ADD_TO_ORDER_BUTTON)-\(item.id)")
        }
    }
}
