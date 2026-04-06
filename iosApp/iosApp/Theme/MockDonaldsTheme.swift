import SwiftUI

// MockDonalds Dark Theme — matches Compose design system (Color.kt + Theme.kt)
enum MockDonaldsColors {
    // Brand
    static let primary = Color(hex: 0xDB0007)       // MockRed
    static let secondary = Color(hex: 0xFFC72C)     // MockYellow

    // On-colors
    static let onPrimary = Color(hex: 0xFFFFFF)
    static let onSecondary = Color(hex: 0x3F2E00)

    // Background & Surface
    static let background = Color(hex: 0x131313)     // DeepObsidian / SurfaceBase
    static let onBackground = Color(hex: 0xFFFFFF)   // OnSurfacePrimary
    static let surface = Color(hex: 0x131313)         // SurfaceBase
    static let onSurface = Color(hex: 0xFFFFFF)       // OnSurfacePrimary
    static let onSurfaceVariant = Color(hex: 0xB3B3B3) // OnSurfaceSecondary
    static let surfaceContainerLow = Color(hex: 0x1C1C1C)  // SurfaceContainer
    static let surfaceContainerHigh = Color(hex: 0x242424)  // SurfaceContainerHigh
    static let surfaceContainerHighest = Color(hex: 0x2E2E2E) // SurfaceContainerHighest

    // Utility
    static let outline = Color(hex: 0x787878)         // OnSurfaceTertiary
}

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
