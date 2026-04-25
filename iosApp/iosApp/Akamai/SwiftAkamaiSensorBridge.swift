import Foundation
import ComposeApp

// POC default: returns empty so the sensor header is omitted.
// Wire the real Akamai Bot Manager iOS SDK here — add the `AkamaiBMP.xcframework`
// (or the matching SPM package) to iosApp and replace `currentSensorData()` with
// a call into the SDK. No Kotlin changes needed to flip this on.
final class SwiftAkamaiSensorBridge: AkamaiSensorBridge {
    func currentSensorData() -> String {
        ""
    }
}
