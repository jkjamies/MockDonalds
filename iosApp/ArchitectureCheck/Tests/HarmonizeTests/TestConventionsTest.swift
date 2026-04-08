import Harmonize
import HarmonizeSemantics
import XCTest

/// Validates iOS test conventions (robot pattern with Swift Testing):
/// - Every *View.swift has a corresponding *ViewTest
/// - Every *ViewTest has a corresponding *ViewRobot
/// - Every *ViewRobot has a corresponding *StateRobot
/// - ViewTest files only reference ViewRobot, not StateRobot directly
/// - StateRobots extend BaseStateRobot
/// - ViewRobots are final classes that compose a StateRobot
/// - ViewTests are @Suite structs with @Test methods
/// - Test code does not contain print statements
final class TestConventionsTest: XCTestCase {

    private lazy var productionScope: HarmonizeScope = Harmonize.productionCode()
        .on("iosApp/iosApp/Features")

    private lazy var testScope: HarmonizeScope = Harmonize.testCode()

    // MARK: - Coverage

    func testEveryViewHasAViewTest() {
        let viewNames = productionScope.sources().compactMap { source -> String? in
            let fileName = source.fileName?.replacingOccurrences(of: ".swift", with: "")
            guard let view = source.structs().first(where: { $0.name == fileName }) else { return nil }
            return view.name.replacingOccurrences(of: "View", with: "")
        }

        XCTAssertTrue(viewNames.isNotEmpty, "Expected to find View structs")

        let testNames = testScope.structs()
            .withNameEndingWith("ViewTest")
            .map { $0.name.replacingOccurrences(of: "ViewTest", with: "") }

        let missing = viewNames.filter { !testNames.contains($0) }

        XCTAssertTrue(
            missing.isEmpty,
            "Views missing ViewTest:\n\(missing.map { "  \($0)View -> expected \($0)ViewTest" }.joined(separator: "\n"))"
        )
    }

    func testEveryViewTestHasAViewRobot() {
        let testNames = testScope.structs()
            .withNameEndingWith("ViewTest")
            .map { $0.name.replacingOccurrences(of: "ViewTest", with: "") }

        XCTAssertTrue(testNames.isNotEmpty, "Expected to find ViewTest structs")

        let robotNames = testScope.classes()
            .withNameEndingWith("ViewRobot")
            .map { $0.name.replacingOccurrences(of: "ViewRobot", with: "") }

        let missing = testNames.filter { !robotNames.contains($0) }

        XCTAssertTrue(
            missing.isEmpty,
            "ViewTests missing ViewRobot:\n\(missing.map { "  \($0)ViewTest -> expected \($0)ViewRobot" }.joined(separator: "\n"))"
        )
    }

    func testEveryViewRobotHasAStateRobot() {
        let robotNames = testScope.classes()
            .withNameEndingWith("ViewRobot")
            .map { $0.name.replacingOccurrences(of: "ViewRobot", with: "") }

        XCTAssertTrue(robotNames.isNotEmpty, "Expected to find ViewRobot classes")

        let stateRobotNames = testScope.classes()
            .withNameEndingWith("StateRobot")
            .withoutNameContaining("Base")
            .map { $0.name.replacingOccurrences(of: "StateRobot", with: "") }

        let missing = robotNames.filter { !stateRobotNames.contains($0) }

        XCTAssertTrue(
            missing.isEmpty,
            "ViewRobots missing StateRobot:\n\(missing.map { "  \($0)ViewRobot -> expected \($0)StateRobot" }.joined(separator: "\n"))"
        )
    }

    // MARK: - Encapsulation

    func testViewTestsOnlyReferenceViewRobot() {
        let viewTestSources = testScope.sources()
            .filter { $0.structs().contains(where: { $0.name.hasSuffix("ViewTest") }) }

        let violators = viewTestSources.filter { $0.source.contains("StateRobot") }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTest must only use ViewRobot — StateRobot is an implementation detail:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    // MARK: - Structure

    func testViewRobotsAreFinalClasses() {
        let viewRobots = testScope.classes()
            .withNameEndingWith("ViewRobot")

        XCTAssertTrue(viewRobots.isNotEmpty, "Expected to find ViewRobot classes")

        let violators = viewRobots.filter { !$0.modifiers.contains(.final) }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobots must be final classes:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    func testViewRobotsComposeAStateRobot() {
        let viewRobots = testScope.classes()
            .withNameEndingWith("ViewRobot")

        XCTAssertTrue(viewRobots.isNotEmpty, "Expected to find ViewRobot classes")

        let violators = viewRobots.filter { robot in
            !robot.variables.contains(where: { $0.name == "stateRobot" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobots must compose a StateRobot via a 'stateRobot' property:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    // MARK: - Inheritance

    func testStateRobotsExtendBaseStateRobot() {
        let stateRobots = testScope.classes()
            .withNameEndingWith("StateRobot")
            .withoutNameContaining("Base")

        XCTAssertTrue(stateRobots.isNotEmpty, "Expected to find StateRobot classes")

        let violators = stateRobots.filter { robot in
            !robot.inheritanceTypesNames.contains(where: { $0.hasPrefix("BaseStateRobot") })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "StateRobots must extend BaseStateRobot:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    // MARK: - Swift Testing Conventions

    func testViewTestsAreSuiteStructs() {
        let viewTests = testScope.structs()
            .withNameEndingWith("ViewTest")

        XCTAssertTrue(viewTests.isNotEmpty, "Expected to find ViewTest structs")

        let violators = viewTests.filter { strct in
            !strct.attributes.contains(where: { $0.name == "Suite" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTest structs must have @Suite attribute:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    func testViewTestsDoNotExtendXCTestCase() {
        let xcTestViewTests = testScope.classes()
            .withNameEndingWith("ViewTest")
            .filter { $0.inheritanceTypesNames.contains("XCTestCase") }

        XCTAssertTrue(
            xcTestViewTests.isEmpty,
            "ViewTests must use @Suite struct, not XCTestCase:\n\(xcTestViewTests.map { $0.name }.joined(separator: "\n"))"
        )
    }

    func testTestCodeDoesNotImportXCTest() {
        let testSources = testScope.sources()
            .filter { source in
                source.structs().contains(where: { $0.name.hasSuffix("ViewTest") }) ||
                source.classes().contains(where: {
                    $0.name.hasSuffix("ViewRobot") || $0.name.hasSuffix("StateRobot")
                })
            }

        let violators = testSources.filter { $0.source.contains("import XCTest") }

        XCTAssertTrue(
            violators.isEmpty,
            "Test code must use Swift Testing (import Testing), not XCTest:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewTestSuitesContainTestMethods() {
        let viewTests = testScope.structs()
            .withNameEndingWith("ViewTest")

        XCTAssertTrue(viewTests.isNotEmpty, "Expected to find ViewTest structs")

        let violators = viewTests.filter { strct in
            !strct.functions.contains(where: { $0.attributes.contains(where: { $0.name == "Test" }) })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTest suites must contain @Test methods:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    // MARK: - MainActor

    func testViewRobotsAreMainActor() {
        let viewRobots = testScope.classes()
            .withNameEndingWith("ViewRobot")

        XCTAssertTrue(viewRobots.isNotEmpty, "Expected to find ViewRobot classes")

        let violators = viewRobots.filter { robot in
            !robot.attributes.contains(where: { $0.name == "MainActor" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobots must be @MainActor for ViewInspector thread safety:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    func testViewTestSuitesAreMainActor() {
        let viewTests = testScope.structs()
            .withNameEndingWith("ViewTest")

        XCTAssertTrue(viewTests.isNotEmpty, "Expected to find ViewTest structs")

        let violators = viewTests.filter { strct in
            !strct.attributes.contains(where: { $0.name == "MainActor" })
        }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTest suites must be @MainActor for ViewInspector thread safety:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    // MARK: - ViewInspector

    func testViewRobotsImportViewInspector() {
        let viewRobotSources = testScope.sources()
            .filter { source in
                source.classes().contains(where: { $0.name.hasSuffix("ViewRobot") })
            }

        let violators = viewRobotSources.filter { !$0.source.contains("import ViewInspector") }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobot files must import ViewInspector for real view hierarchy assertions:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    // MARK: - Landscape Testing

    func testViewRobotsHaveCreateLandscapeView() {
        let viewRobotSources = testScope.sources()
            .filter { source in
                source.classes().contains(where: { $0.name.hasSuffix("ViewRobot") })
            }

        let violators = viewRobotSources.filter { !$0.source.contains("createLandscapeView") }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobots must have a createLandscapeView() method for landscape testing:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewRobotsHaveAssertLandscapeScreen() {
        let viewRobotSources = testScope.sources()
            .filter { source in
                source.classes().contains(where: { $0.name.hasSuffix("ViewRobot") })
            }

        let violators = viewRobotSources.filter { !$0.source.contains("assertLandscapeScreen") }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewRobots must have an assertLandscapeScreen() method for landscape testing:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testViewTestsHaveRendersLandscapeLayout() {
        let viewTestSources = testScope.sources()
            .filter { source in
                source.structs().contains(where: { $0.name.hasSuffix("ViewTest") })
            }

        let violators = viewTestSources.filter { !$0.source.contains("rendersLandscapeLayout") }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTests must have a rendersLandscapeLayout test for landscape coverage:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    // MARK: - Hygiene

    func testTestCodeDoesNotContainPrintStatements() {
        let testSources = testScope.sources()
            .filter { source in
                source.structs().contains(where: { $0.name.hasSuffix("ViewTest") }) ||
                source.classes().contains(where: {
                    $0.name.hasSuffix("ViewRobot") || $0.name.hasSuffix("StateRobot")
                })
            }

        let violators = testSources.filter { $0.source.contains("print(") }

        XCTAssertTrue(
            violators.isEmpty,
            "Test code must not contain print statements:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testTestSuitesEndWithTest() {
        let testSuites = testScope.structs()
            .filter { $0.attributes.contains(where: { $0.name == "Suite" }) }

        testSuites.assertTrue(
            message: "Test suites with @Suite must end with 'Test'"
        ) { strct in
            strct.name.hasSuffix("Test")
        }
    }
}
