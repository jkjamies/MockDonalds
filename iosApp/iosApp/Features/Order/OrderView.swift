import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = OrderTestTags.shared

@CircuitInject(OrderScreen.self, OrderUiState.self)
struct OrderView: View {
    @Environment(\.samplePlatterColors) private var colors

    let state: OrderUiState

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: PlatterDimens.spacingLg) {
                    ForEach(state.categoryPreviews, id: \.id) { preview in
                        CategoryPreviewCardView(
                            name: preview.name,
                            firstItemImageUrl: preview.firstItemImageUrl,
                            onTap: {
                                state.eventSink(
                                    OrderEvent.CategoryTapped(id: preview.id)
                                )
                            }
                        )
                        .accessibilityIdentifier(
                            "\(tags.CATEGORY_PREVIEW_CARD)-\(preview.id)"
                        )
                    }
                }
                .padding(.horizontal, PlatterDimens.spacingXl)
                .padding(.vertical, PlatterDimens.spacingXl)
                .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: false))
            }

            cartBar
        }
        .background(colors.background)
    }

    @ViewBuilder
    private var cartBar: some View {
        if let cart = state.cartSummary {
            OrderCartBarView(
                cart: cart,
                onTap: { state.eventSink(OrderEvent.CartClicked()) }
            )
            .accessibilityIdentifier(tags.CART_BAR)
        }
    }
}

struct CategoryPreviewCardView: View {
    @Environment(\.samplePlatterColors) private var colors

    let name: String
    let firstItemImageUrl: String?
    var onTap: () -> Void = {}

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            Color.clear
                .aspectRatio(2.0, contentMode: .fit)
                .overlay {
                    if let url = firstItemImageUrl, !url.isEmpty {
                        AsyncImage(
                            url: URL(string: url),
                            content: { image in
                                image.resizable()
                                    .aspectRatio(contentMode: .fill)
                            },
                            placeholder: {
                                colors.surfaceContainerLow
                            }
                        )
                    } else {
                        colors.surfaceContainerLow
                    }
                }
                .clipped()

            LinearGradient(
                colors: [Color.clear, Color.black.opacity(0.6)],
                startPoint: .top,
                endPoint: .bottom
            )

            Text(name)
                .font(.system(size: 28, weight: .black))
                .foregroundColor(.white)
                .padding(PlatterDimens.spacingXl)
        }
        .cornerRadius(PlatterDimens.radiusMd)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

struct OrderCartBarView: View {
    @Environment(\.samplePlatterColors) private var colors

    let cart: CartSummary
    var onTap: () -> Void = {}

    var body: some View {
        HStack {
            HStack(spacing: PlatterDimens.spacingMd) {
                Circle()
                    .fill(colors.primaryDarker.opacity(0.2))
                    .frame(width: PlatterDimens.spacingXxl, height: PlatterDimens.spacingXxl)
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
            HStack(spacing: PlatterDimens.spacingSm) {
                Text(cart.total)
                    .font(.title3)
                    .fontWeight(.black)
                    .foregroundColor(colors.onPrimaryButton)
                Text("->")
                    .fontWeight(.bold)
                    .foregroundColor(colors.onPrimaryButton)
            }
        }
        .padding(.horizontal, PlatterDimens.spacingXl)
        .padding(.vertical, PlatterDimens.spacingLg)
        .background(colors.primary)
        .cornerRadius(PlatterDimens.radiusMd)
        .padding(.horizontal, PlatterDimens.spacingXl)
        .padding(.bottom, 88)
        .onTapGesture(perform: onTap)
    }
}
