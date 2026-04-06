import SwiftUI
import ComposeApp

struct ScanView: View {
    let state: ScanUiState

    var body: some View {
        ScrollView {
            VStack(spacing: 40) {
                // Main QR Code Card
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
                            .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                            .padding(.bottom, 32)

                        // QR Code with gradient border
                        ZStack {
                            AngularGradient(
                                gradient: Gradient(stops: [
                                    .init(color: MockDonaldsColors.primary, location: 0),
                                    .init(color: MockDonaldsColors.secondary, location: 0.45),
                                    .init(color: MockDonaldsColors.secondary, location: 0.65),
                                    .init(color: MockDonaldsColors.primary, location: 1),
                                ]),
                                center: .center
                            )
                            .frame(width: 260, height: 260)
                            .clipShape(RoundedRectangle(cornerRadius: 16))

                            AsyncImage(url: URL(string: "https://lh3.googleusercontent.com/aida-public/AB6AXuC9Pojw6DdMsOOR6hCze-e8NXeAre3ygPVczci3TVq7UnAnPDxoxM_GJQysSal74SZsWTa2Eli6wrej9xa6D_JnOd9cFYjPNapwY2oPFt_4y1988l-6Smo9p3_7Tm1cpbycujNr-US0sB3HayQD2AbCIjUc93yNVTN8VNhZknndgmID66Z92VP8jVgZ_SLb4zLUb_TqSBcfwJX6CiG_OZpDr9dNsM-Av6tOdOBkuZixKo_kctR9aeyVVf9scLxlreCGNXUSrK3bdpw")) { image in
                                image.resizable().aspectRatio(contentMode: .fit)
                            } placeholder: {
                                MockDonaldsColors.surfaceContainerHighest
                            }
                            .frame(width: 252, height: 252)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                        }

                        HStack(spacing: 12) {
                            Text("\u{2B50}")
                                .foregroundColor(MockDonaldsColors.secondary)
                            Text("Current Member")
                                .font(.title3)
                                .fontWeight(.heavy)
                                .foregroundColor(MockDonaldsColors.onSurface)
                        }
                        .padding(.top, 32)
                    }
                    .padding(32)
                }
                .background(MockDonaldsColors.surfaceContainerLow)
                .cornerRadius(12)

                // Rewards Progress
                VStack(spacing: 16) {
                    HStack(alignment: .bottom) {
                        Text("REWARDS PROGRESS")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .tracking(1)
                            .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                        Spacer()
                        HStack(alignment: .bottom, spacing: 4) {
                            Text("750")
                                .font(.title3)
                                .fontWeight(.black)
                                .foregroundColor(MockDonaldsColors.secondary)
                            Text("PTS")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                        }
                    }

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule().fill(MockDonaldsColors.surfaceContainerHighest).frame(height: 8)
                            Capsule()
                                .fill(
                                    LinearGradient(
                                        colors: [MockDonaldsColors.primary, MockDonaldsColors.secondary],
                                        startPoint: .leading, endPoint: .trailing
                                    )
                                )
                                .frame(width: geo.size.width * 0.75, height: 8)
                        }
                    }
                    .frame(height: 8)

                    Text("You're just 250 pts away from your next free treat!")
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(MockDonaldsColors.onSurface.opacity(0.8))
                }

                // Action Buttons
                HStack(spacing: 16) {
                    Button(action: {}) {
                        HStack(spacing: 12) {
                            Text("\u{1F4B3}").foregroundColor(MockDonaldsColors.secondary)
                            Text("Pay Now").font(.caption).fontWeight(.bold).foregroundColor(MockDonaldsColors.onSurface)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                        .background(MockDonaldsColors.surfaceContainerHigh)
                        .cornerRadius(12)
                    }
                    Button(action: {}) {
                        HStack(spacing: 12) {
                            Text("\u{1F3F7}\u{FE0F}").foregroundColor(MockDonaldsColors.secondary)
                            Text("View Offers").font(.caption).fontWeight(.bold).foregroundColor(MockDonaldsColors.onSurface)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                        .background(MockDonaldsColors.surfaceContainerHigh)
                        .cornerRadius(12)
                    }
                }

                // Pro Tip
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
                        Text("Ensure your screen brightness is turned up for the best scanning experience at our kiosks.")
                            .font(.caption)
                            .foregroundColor(MockDonaldsColors.onSurfaceVariant)
                    }
                }
                .padding(20)
                .background(MockDonaldsColors.surfaceContainerLow)
                .cornerRadius(12)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(MockDonaldsColors.background)
    }
}
