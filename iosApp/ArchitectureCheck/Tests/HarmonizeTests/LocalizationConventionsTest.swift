import Foundation
import XCTest

/// Validates iOS localization layout:
/// - All `.strings` files must live under `iosApp/iosApp/Resources/{locale}.lproj/`.
/// - The Phrase `pullTranslations` Gradle task is the only writer for these files;
///   stray `Localizable.strings` elsewhere in the project means a contributor
///   bypassed Phrase and will desync from the canonical translation source.
final class LocalizationConventionsTest: XCTestCase {

    private let projectRoot: URL = {
        var dir = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
        while dir.path != "/" {
            if FileManager.default.fileExists(atPath: dir.appendingPathComponent("settings.gradle.kts").path) {
                return dir
            }
            dir = dir.deletingLastPathComponent()
        }
        XCTFail("Could not locate project root from \(#filePath)")
        return URL(fileURLWithPath: "/")
    }()

    func testStringsFilesLiveUnderResourcesLproj() throws {
        let iosAppDir = projectRoot.appendingPathComponent("iosApp/iosApp")
        let canonicalRoot = iosAppDir.appendingPathComponent("Resources").path

        let enumerator = FileManager.default.enumerator(
            at: iosAppDir,
            includingPropertiesForKeys: [.isRegularFileKey],
            options: [.skipsHiddenFiles]
        )

        var violators: [String] = []
        while let url = enumerator?.nextObject() as? URL {
            guard url.pathExtension == "strings" else { continue }
            let parent = url.deletingLastPathComponent()
            let parentName = parent.lastPathComponent
            let underResources = url.path.hasPrefix(canonicalRoot + "/")
            let inLproj = parentName.hasSuffix(".lproj")
            if !(underResources && inLproj) {
                violators.append(url.path.replacingOccurrences(of: projectRoot.path + "/", with: ""))
            }
        }

        XCTAssertTrue(
            violators.isEmpty,
            "All `.strings` files must live under `iosApp/iosApp/Resources/{locale}.lproj/` " +
            "(written by the `pullTranslations` Gradle task). Stray locations bypass Phrase:\n" +
            violators.map { "  \($0)" }.joined(separator: "\n")
        )
    }
}
