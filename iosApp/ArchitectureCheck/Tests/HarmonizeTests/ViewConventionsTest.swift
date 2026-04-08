import Harmonize
import HarmonizeSemantics
import XCTest

/// Validates iOS view conventions:
/// - View structs conform to View protocol
/// - Every View struct imports ComposeApp for shared KMP state
/// - Every View struct has a `state` property (the UiState from shared code)
/// - Views do not import UIKit (pure SwiftUI)
/// - Views do not contain force unwraps, force casts, or force try
/// - Views do not use Combine or DispatchQueue (async/await only)
/// - Views do not contain print statements
/// - Views do not contain TODO/FIXME/HACK comments
/// - Every View uses accessibilityIdentifier (shared TestTags from KMP)
final class ViewConventionsTest: XCTestCase {

    private lazy var scope: HarmonizeScope = Harmonize.productionCode()
        .on("iosApp/iosApp/Features")

    /// Primary View structs — one per feature file, matching the file name pattern *View.swift
    private lazy var viewStructs: [Struct] = scope.sources().compactMap { source in
        let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
        return source.structs().first(where: { $0.name == fileName })
    }

    // MARK: - Structure

    func testViewStructsConformToView() {
        XCTAssertTrue(viewStructs.isNotEmpty, "Expected to find View structs")

        viewStructs.assertTrue(
            message: "View structs must conform to the View protocol"
        ) { view in
            view.inheritanceTypesNames.contains("View")
        }
    }

    func testViewStructsImportComposeApp() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        XCTAssertTrue(sources.isNotEmpty, "Expected to find View source files")

        let violators = sources.filter { source in
            !source.imports().contains(where: { $0.name == "ComposeApp" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must import ComposeApp for shared KMP state:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewStructsHaveStateProperty() {
        XCTAssertTrue(viewStructs.isNotEmpty, "Expected to find View structs")

        viewStructs.assertTrue(
            message: "View structs must have a 'state' property for shared UiState"
        ) { view in
            view.variables.contains(where: { $0.name == "state" })
        }
    }

    // MARK: - Accessibility

    func testViewsUseAccessibilityIdentifiers() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter { !$0.source.contains("accessibilityIdentifier") }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must use accessibilityIdentifier for UI testing:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewsDoNotImportUIKit() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter { source in
            source.imports().contains(where: { $0.name == "UIKit" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must use SwiftUI only — do not import UIKit:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    // MARK: - Safety

    func testViewsDoNotContainForceUnwraps() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter { source in
            // Check for implicitly unwrapped optional declarations (e.g. var x: String!)
            let hasImplicitUnwrap = source.structs().flatMap { $0.variables }.contains { variable in
                variable.typeAnnotation?.name.contains("!") == true
            }
            return hasImplicitUnwrap || source.source.contains("as!") || source.source.contains("try!")
        }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must not use force unwraps, force casts (as!), or force try (try!):\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewsDoNotUseCombineOrDispatchQueue() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let combineViolators = sources.filter { source in
            source.imports().contains(where: { $0.name == "Combine" })
        }

        let dispatchViolators = sources.filter { $0.source.contains("DispatchQueue") }

        let violators = (combineViolators + dispatchViolators).map { $0.fileName ?? "unknown" }
        let unique = Array(Set(violators))

        XCTAssertTrue(
            unique.isEmpty,
            "Views must use async/await only — no Combine or DispatchQueue:\n\(unique.joined(separator: "\n"))"
        )
    }

    // MARK: - Hygiene

    func testViewsDoNotContainPrintStatements() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter { $0.source.contains("print(") }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must not contain print statements — use os_log or remove:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewsDoNotContainTodoComments() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter {
            $0.source.contains("TODO") || $0.source.contains("FIXME") || $0.source.contains("HACK")
        }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must not contain TODO/FIXME/HACK comments — resolve before merging:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewsUseSharedTestTags() {
        let sources = scope.sources()
            .filter { source in
                let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
                return source.structs().contains(where: { $0.name == fileName })
            }

        let violators = sources.filter {
            $0.source.contains("accessibilityIdentifier") && !$0.source.contains("TestTags")
        }

        XCTAssertTrue(
            violators.isEmpty,
            "Views must use shared KMP TestTags for accessibility identifiers, not hardcoded strings:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }
}
