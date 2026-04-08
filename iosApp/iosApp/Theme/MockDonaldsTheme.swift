import SwiftUI

// MARK: - Design Tokens (Spacing, Radii, Sizes)

enum MockDimens {
    // Spacing scale
    static let spacingXs: CGFloat = 4
    static let spacingSm: CGFloat = 8
    static let spacingMd: CGFloat = 12
    static let spacingLg: CGFloat = 16
    static let spacingXl: CGFloat = 24
    static let spacingXxl: CGFloat = 32
    static let spacingXxxl: CGFloat = 48

    // Corner radii
    static let radiusSm: CGFloat = 6
    static let radiusMd: CGFloat = 12
    static let radiusLg: CGFloat = 16

    // Component sizes
    static let heroHeight: CGFloat = 480
    static let cardWidth: CGFloat = 288
    static let cardHeight: CGFloat = 176
    static let thumbnailHeight: CGFloat = 160
    static let iconLg: CGFloat = 48
    static let iconMd: CGFloat = 40
    static let bottomNavHeight: CGFloat = 80
    static let bottomBarPadding: CGFloat = 128
}

// MARK: - Color System (Light + Dark)

struct MockDonaldsColorScheme {
    // Brand
    let primary: Color
    let secondary: Color

    // On-colors
    let onPrimary: Color
    let onSecondary: Color

    // Background & Surface
    let background: Color
    let onBackground: Color
    let surface: Color
    let onSurface: Color
    let onSurfaceVariant: Color
    let surfaceContainerLow: Color
    let surfaceContainerHigh: Color
    let surfaceContainerHighest: Color

    // Utility
    let outline: Color

    // Extended (brand accents not in M3)
    let primaryDark: Color
    let primaryDarker: Color
    let onPrimaryButton: Color
    let onSecondaryTag: Color
    let onSecondaryContainer: Color
    let secondaryLight: Color
}

private let darkScheme = MockDonaldsColorScheme(
    primary: Color(hex: 0xDB0007),
    secondary: Color(hex: 0xFFC72C),
    onPrimary: Color(hex: 0xFFFFFF),
    onSecondary: Color(hex: 0x584200),
    background: Color(hex: 0x131313),
    onBackground: Color(hex: 0xFFFFFF),
    surface: Color(hex: 0x131313),
    onSurface: Color(hex: 0xFFFFFF),
    onSurfaceVariant: Color(hex: 0xB3B3B3),
    surfaceContainerLow: Color(hex: 0x1C1C1C),
    surfaceContainerHigh: Color(hex: 0x242424),
    surfaceContainerHighest: Color(hex: 0x2E2E2E),
    outline: Color(hex: 0x787878),
    primaryDark: Color(hex: 0x930003),
    primaryDarker: Color(hex: 0x690001),
    onPrimaryButton: Color(hex: 0xFFEBE8),
    onSecondaryTag: Color(hex: 0x584200),
    onSecondaryContainer: Color(hex: 0x3F2E00),
    secondaryLight: Color(hex: 0xFFDF99)
)

private let lightScheme = MockDonaldsColorScheme(
    primary: Color(hex: 0xDB0007),
    secondary: Color(hex: 0xFFC72C),
    onPrimary: .white,
    onSecondary: Color(hex: 0x584200),
    background: Color(hex: 0xFFFBFF),
    onBackground: Color(hex: 0x1C1B1F),
    surface: Color(hex: 0xFFFBFF),
    onSurface: Color(hex: 0x1C1B1F),
    onSurfaceVariant: Color(hex: 0x49454F),
    surfaceContainerLow: Color(hex: 0xF5F1EC),
    surfaceContainerHigh: Color(hex: 0xEDE8E2),
    surfaceContainerHighest: Color(hex: 0xE8E0D8),
    outline: Color(hex: 0x79747E),
    primaryDark: Color(hex: 0x930003),
    primaryDarker: Color(hex: 0x690001),
    onPrimaryButton: Color(hex: 0xFFEBE8),
    onSecondaryTag: Color(hex: 0x584200),
    onSecondaryContainer: Color(hex: 0x3F2E00),
    secondaryLight: Color(hex: 0xB8860B)
)

// MARK: - Environment Key

private struct MockDonaldsColorsKey: EnvironmentKey {
    static let defaultValue: MockDonaldsColorScheme = darkScheme
}

extension EnvironmentValues {
    var mockDonaldsColors: MockDonaldsColorScheme {
        get { self[MockDonaldsColorsKey.self] }
        set { self[MockDonaldsColorsKey.self] = newValue }
    }
}

// MARK: - Theme Modifier

struct MockDonaldsTheme: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content
            .environment(\.mockDonaldsColors, colorScheme == .dark ? darkScheme : lightScheme)
    }
}

extension View {
    func mockDonaldsTheme() -> some View {
        modifier(MockDonaldsTheme())
    }
}

// MARK: - Static Convenience (backward compat during migration)

enum MockDonaldsColors {
    // Brand
    static let primary = Color(hex: 0xDB0007)
    static let secondary = Color(hex: 0xFFC72C)

    // On-colors
    static let onPrimary = Color(hex: 0xFFFFFF)
    static let onSecondary = Color(hex: 0x3F2E00)

    // Background & Surface
    static let background = Color(hex: 0x131313)
    static let onBackground = Color(hex: 0xFFFFFF)
    static let surface = Color(hex: 0x131313)
    static let onSurface = Color(hex: 0xFFFFFF)
    static let onSurfaceVariant = Color(hex: 0xB3B3B3)
    static let surfaceContainerLow = Color(hex: 0x1C1C1C)
    static let surfaceContainerHigh = Color(hex: 0x242424)
    static let surfaceContainerHighest = Color(hex: 0x2E2E2E)

    // Utility
    static let outline = Color(hex: 0x787878)
}

// MARK: - Color Hex Extension

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
