import SwiftUI

extension MockDimens {
    static func adaptiveHeroHeight(isLandscape: Bool) -> CGFloat { isLandscape ? 240 : heroHeight }
    static func adaptiveQrCodeSize(isLandscape: Bool) -> CGFloat { isLandscape ? 180 : 252 }
    static func adaptiveBottomBarPadding(isLandscape: Bool) -> CGFloat { isLandscape ? 72 : bottomBarPadding }
    static func adaptiveBottomNavHeight(isLandscape: Bool) -> CGFloat { isLandscape ? 56 : bottomNavHeight }
}
