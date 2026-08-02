import CircuitMacros
import SwiftUI
import ComposeApp

private let tags = ScanTestTags.shared

@CircuitInject(ScanScreen.self, ScanUiState.self)
struct ScanView: View {
    let state: ScanUiState
    @State private var gradientAngle: Double = 0
    @Environment(\.samplePlatterColors) private var colors
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    private var isLandscape: Bool { verticalSizeClass == .compact }

    var body: some View {
        ScrollView {
            if isLandscape {
                // Two-column: QR card left, supplementary right
                HStack(alignment: .top, spacing: PlatterDimens.spacingXl) {
                    memberCard
                        .frame(maxWidth: .infinity)
                    VStack(spacing: PlatterDimens.spacingXl) {
                        rewardsProgressSection
                        actionButtons
                        proTipSection
                    }
                    .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, PlatterDimens.spacingXl)
                .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: true))
            } else {
                VStack(spacing: 40) {
                    memberCard
                    rewardsProgressSection
                    actionButtons
                    proTipSection
                }
                .padding(.horizontal, PlatterDimens.spacingXl)
                .padding(.bottom, PlatterDimens.adaptiveBottomBarPadding(isLandscape: false))
            }
        }
        .background(colors.background)
    }

    @ViewBuilder
    private var memberCard: some View {
        if let member = state.memberInfo {
            ZStack {
                VStack(spacing: 0) {
                    Text("MOCK REWARDS")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                        .padding(.bottom, PlatterDimens.spacingSm)
                    Text("Scan at the counter to earn & redeem")
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(
                            colors.onSurfaceVariant
                        )
                        .padding(.bottom, PlatterDimens.spacingXxl)

                    qrCodeView(member: member)

                    HStack(spacing: PlatterDimens.spacingMd) {
                        Text("\u{2B50}")
                            .foregroundColor(
                                colors.secondary
                            )
                        Text(member.memberStatus)
                            .font(.title3)
                            .fontWeight(.heavy)
                            .foregroundColor(
                                colors.onSurface
                            )
                    }
                    .padding(.top, PlatterDimens.spacingXxl)
                }
                .padding(PlatterDimens.spacingXxl)
            }
            .background(colors.surfaceContainerLow)
            .cornerRadius(PlatterDimens.radiusMd)
            .accessibilityIdentifier(tags.MEMBER_CARD)
        }
    }

    private func qrCodeView(member: MemberInfo) -> some View {
        ZStack {
            AngularGradient(
                gradient: Gradient(stops: [
                    .init(
                        color: colors.primary,
                        location: 0
                    ),
                    .init(
                        color: colors.secondary,
                        location: 0.45
                    ),
                    .init(
                        color: colors.secondary,
                        location: 0.65
                    ),
                    .init(
                        color: colors.primary,
                        location: 1
                    ),
                ]),
                center: .center,
                angle: .degrees(gradientAngle)
            )
            .frame(width: 260, height: 260)
            .clipShape(RoundedRectangle(cornerRadius: PlatterDimens.radiusLg))
            .onAppear {
                withAnimation(
                    .linear(duration: 3)
                        .repeatForever(autoreverses: false)
                ) {
                    gradientAngle = 360
                }
            }

            AsyncImage(
                url: URL(string: member.qrCodeUrl),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fit)
                },
                placeholder: {
                    colors.surfaceContainerHighest
                }
            )
            .frame(width: 252, height: 252)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    @ViewBuilder
    private var rewardsProgressSection: some View {
        if let progress = state.rewardsProgress {
            VStack(spacing: PlatterDimens.spacingLg) {
                HStack(alignment: .bottom) {
                    Text("REWARDS PROGRESS")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .tracking(1)
                        .foregroundColor(
                            colors.onSurfaceVariant
                        )
                    Spacer()
                    HStack(alignment: .bottom, spacing: PlatterDimens.spacingXs) {
                        Text("\(progress.currentPoints)")
                            .font(.title3)
                            .fontWeight(.black)
                            .foregroundColor(
                                colors.secondary
                            )
                        Text("PTS")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundColor(
                                colors.onSurfaceVariant
                            )
                    }
                }

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(
                                colors
                                    .surfaceContainerHighest
                            )
                            .frame(height: PlatterDimens.spacingSm)
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
                                    * CGFloat(
                                        progress.progressFraction
                                    ),
                                height: PlatterDimens.spacingSm
                            )
                    }
                }
                .frame(height: PlatterDimens.spacingSm)

                Text(progress.message)
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(
                        colors.onSurface.opacity(0.8)
                    )
            }
            .accessibilityIdentifier(tags.REWARDS_PROGRESS)
        }
    }

    private var actionButtons: some View {
        HStack(spacing: PlatterDimens.spacingLg) {
            Button(
                action: {
                    state.eventSink(ScanEvent.PayNowClicked())
                },
                label: {
                    HStack(spacing: PlatterDimens.spacingMd) {
                        Text("\u{1F4B3}")
                            .foregroundColor(
                                colors.secondary
                            )
                        Text("Pay Now")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(
                                colors.onSurface
                            )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(
                        colors.surfaceContainerHigh
                    )
                    .cornerRadius(PlatterDimens.radiusMd)
                }
            )
            .accessibilityIdentifier(tags.PAY_NOW_BUTTON)
            Button(
                action: {
                    state.eventSink(
                        ScanEvent.ViewOffersClicked()
                    )
                },
                label: {
                    HStack(spacing: PlatterDimens.spacingMd) {
                        Text("\u{1F3F7}\u{FE0F}")
                            .foregroundColor(
                                colors.secondary
                            )
                        Text("View Offers")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(
                                colors.onSurface
                            )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(
                        colors.surfaceContainerHigh
                    )
                    .cornerRadius(PlatterDimens.radiusMd)
                }
            )
            .accessibilityIdentifier(tags.VIEW_OFFERS_BUTTON)
        }
    }

    private var proTipSection: some View {
        HStack(alignment: .top, spacing: PlatterDimens.spacingLg) {
            Circle()
                .fill(colors.surfaceContainerHighest)
                .frame(width: PlatterDimens.iconMd, height: PlatterDimens.iconMd)
                .overlay(Text("\u{2139}\u{FE0F}"))
            VStack(alignment: .leading, spacing: PlatterDimens.spacingXs) {
                Text("Pro Tip")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colors.onSurface)
                Text(
                    "Ensure your screen brightness is turned "
                    + "up for the best scanning experience "
                    + "at our kiosks."
                )
                .font(.caption)
                .foregroundColor(
                    colors.onSurfaceVariant
                )
            }
        }
        .padding(20)
        .background(colors.surfaceContainerLow)
        .cornerRadius(PlatterDimens.radiusMd)
        .accessibilityIdentifier(tags.PRO_TIP)
    }
}
