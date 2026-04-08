import SwiftUI
import ComposeApp

private let tags = ScanTestTags.shared

struct ScanView: View {
    let state: ScanUiState
    @State private var gradientAngle: Double = 0
    @Environment(\.mockDonaldsColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: 40) {
                memberCard
                rewardsProgressSection
                actionButtons
                proTipSection
            }
            .padding(.horizontal, MockDimens.spacingXl)
            .padding(.bottom, MockDimens.bottomBarPadding)
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
                        .padding(.bottom, MockDimens.spacingSm)
                    Text("Scan at the counter to earn & redeem")
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(
                            colors.onSurfaceVariant
                        )
                        .padding(.bottom, MockDimens.spacingXxl)

                    qrCodeView(member: member)

                    HStack(spacing: MockDimens.spacingMd) {
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
                    .padding(.top, MockDimens.spacingXxl)
                }
                .padding(MockDimens.spacingXxl)
            }
            .background(colors.surfaceContainerLow)
            .cornerRadius(MockDimens.radiusMd)
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
            .clipShape(RoundedRectangle(cornerRadius: MockDimens.radiusLg))
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
            VStack(spacing: MockDimens.spacingLg) {
                HStack(alignment: .bottom) {
                    Text("REWARDS PROGRESS")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .tracking(1)
                        .foregroundColor(
                            colors.onSurfaceVariant
                        )
                    Spacer()
                    HStack(alignment: .bottom, spacing: MockDimens.spacingXs) {
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
                            .frame(height: MockDimens.spacingSm)
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
                                height: MockDimens.spacingSm
                            )
                    }
                }
                .frame(height: MockDimens.spacingSm)

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
        HStack(spacing: MockDimens.spacingLg) {
            Button(
                action: {
                    state.eventSink(ScanEvent.PayNowClicked())
                },
                label: {
                    HStack(spacing: MockDimens.spacingMd) {
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
                    .cornerRadius(MockDimens.radiusMd)
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
                    HStack(spacing: MockDimens.spacingMd) {
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
                    .cornerRadius(MockDimens.radiusMd)
                }
            )
            .accessibilityIdentifier(tags.VIEW_OFFERS_BUTTON)
        }
    }

    private var proTipSection: some View {
        HStack(alignment: .top, spacing: MockDimens.spacingLg) {
            Circle()
                .fill(colors.surfaceContainerHighest)
                .frame(width: MockDimens.iconMd, height: MockDimens.iconMd)
                .overlay(Text("\u{2139}\u{FE0F}"))
            VStack(alignment: .leading, spacing: MockDimens.spacingXs) {
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
        .cornerRadius(MockDimens.radiusMd)
        .accessibilityIdentifier(tags.PRO_TIP)
    }
}
