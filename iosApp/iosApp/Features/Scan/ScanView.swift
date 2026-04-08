import SwiftUI
import ComposeApp

private let tags = ScanTestTags.shared

struct ScanView: View {
    let state: ScanUiState
    @State private var gradientAngle: Double = 0

    var body: some View {
        ScrollView {
            VStack(spacing: 40) {
                memberCard
                rewardsProgressSection
                actionButtons
                proTipSection
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }

    @ViewBuilder
    private var memberCard: some View {
        if let member = state.memberInfo {
            ZStack {
                VStack(spacing: 0) {
                    Text("MOCK REWARDS")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(MockDonaldsColors.onSurface)
                        .padding(.bottom, 8)
                    Text("Scan at the counter to earn & redeem")
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(
                            MockDonaldsColors.onSurfaceVariant
                        )
                        .padding(.bottom, 32)

                    qrCodeView(member: member)

                    HStack(spacing: 12) {
                        Text("\u{2B50}")
                            .foregroundColor(
                                MockDonaldsColors.secondary
                            )
                        Text(member.memberStatus)
                            .font(.title3)
                            .fontWeight(.heavy)
                            .foregroundColor(
                                MockDonaldsColors.onSurface
                            )
                    }
                    .padding(.top, 32)
                }
                .padding(32)
            }
            .background(MockDonaldsColors.surfaceContainerLow)
            .cornerRadius(12)
            .accessibilityIdentifier(tags.MEMBER_CARD)
        }
    }

    private func qrCodeView(member: MemberInfo) -> some View {
        ZStack {
            AngularGradient(
                gradient: Gradient(stops: [
                    .init(
                        color: MockDonaldsColors.primary,
                        location: 0
                    ),
                    .init(
                        color: MockDonaldsColors.secondary,
                        location: 0.45
                    ),
                    .init(
                        color: MockDonaldsColors.secondary,
                        location: 0.65
                    ),
                    .init(
                        color: MockDonaldsColors.primary,
                        location: 1
                    ),
                ]),
                center: .center,
                angle: .degrees(gradientAngle)
            )
            .frame(width: 260, height: 260)
            .clipShape(RoundedRectangle(cornerRadius: 16))
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
                    MockDonaldsColors.surfaceContainerHighest
                }
            )
            .frame(width: 252, height: 252)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    @ViewBuilder
    private var rewardsProgressSection: some View {
        if let progress = state.rewardsProgress {
            VStack(spacing: 16) {
                HStack(alignment: .bottom) {
                    Text("REWARDS PROGRESS")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .tracking(1)
                        .foregroundColor(
                            MockDonaldsColors.onSurfaceVariant
                        )
                    Spacer()
                    HStack(alignment: .bottom, spacing: 4) {
                        Text("\(progress.currentPoints)")
                            .font(.title3)
                            .fontWeight(.black)
                            .foregroundColor(
                                MockDonaldsColors.secondary
                            )
                        Text("PTS")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundColor(
                                MockDonaldsColors.onSurfaceVariant
                            )
                    }
                }

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(
                                MockDonaldsColors
                                    .surfaceContainerHighest
                            )
                            .frame(height: 8)
                        Capsule()
                            .fill(
                                LinearGradient(
                                    colors: [
                                        MockDonaldsColors.primary,
                                        MockDonaldsColors.secondary,
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
                                height: 8
                            )
                    }
                }
                .frame(height: 8)

                Text(progress.message)
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(
                        MockDonaldsColors.onSurface.opacity(0.8)
                    )
            }
            .accessibilityIdentifier(tags.REWARDS_PROGRESS)
        }
    }

    private var actionButtons: some View {
        HStack(spacing: 16) {
            Button(
                action: {
                    state.eventSink(ScanEvent.PayNowClicked())
                },
                label: {
                    HStack(spacing: 12) {
                        Text("\u{1F4B3}")
                            .foregroundColor(
                                MockDonaldsColors.secondary
                            )
                        Text("Pay Now")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(
                                MockDonaldsColors.onSurface
                            )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(
                        MockDonaldsColors.surfaceContainerHigh
                    )
                    .cornerRadius(12)
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
                    HStack(spacing: 12) {
                        Text("\u{1F3F7}\u{FE0F}")
                            .foregroundColor(
                                MockDonaldsColors.secondary
                            )
                        Text("View Offers")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(
                                MockDonaldsColors.onSurface
                            )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(
                        MockDonaldsColors.surfaceContainerHigh
                    )
                    .cornerRadius(12)
                }
            )
            .accessibilityIdentifier(tags.VIEW_OFFERS_BUTTON)
        }
    }

    private var proTipSection: some View {
        HStack(alignment: .top, spacing: 16) {
            Circle()
                .fill(MockDonaldsColors.surfaceContainerHighest)
                .frame(width: 40, height: 40)
                .overlay(Text("\u{2139}\u{FE0F}"))
            VStack(alignment: .leading, spacing: 4) {
                Text("Pro Tip")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(MockDonaldsColors.onSurface)
                Text(
                    "Ensure your screen brightness is turned "
                    + "up for the best scanning experience "
                    + "at our kiosks."
                )
                .font(.caption)
                .foregroundColor(
                    MockDonaldsColors.onSurfaceVariant
                )
            }
        }
        .padding(20)
        .background(MockDonaldsColors.surfaceContainerLow)
        .cornerRadius(12)
        .accessibilityIdentifier(tags.PRO_TIP)
    }
}
