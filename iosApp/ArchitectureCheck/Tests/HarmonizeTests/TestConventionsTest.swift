import Harmonize
import HarmonizeSemantics
import XCTest

/// Validates iOS test conventions (robot pattern):
/// - Every *View.swift has a corresponding *ViewTest
/// - Every *ViewTest has a corresponding *ViewRobot
/// - Every *ViewRobot has a corresponding *StateRobot
/// - ViewTest files only reference ViewRobot, not StateRobot directly
/// - StateRobots extend BaseStateRobot
/// - ViewRobots are final classes
/// - ViewRobots compose a StateRobot
/// - ViewTest classes are final
/// - ViewTest classes extend XCTestCase
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

        let testNames = testScope.classes()
            .withNameEndingWith("ViewTest")
            .map { $0.name.replacingOccurrences(of: "ViewTest", with: "") }

        let missing = viewNames.filter { !testNames.contains($0) }

        XCTAssertTrue(
            missing.isEmpty,
            "Views missing ViewTest:\n\(missing.map { "  \($0)View -> expected \($0)ViewTest" }.joined(separator: "\n"))"
        )
    }

    func testEveryViewTestHasAViewRobot() {
        let testNames = testScope.classes()
            .withNameEndingWith("ViewTest")
            .map { $0.name.replacingOccurrences(of: "ViewTest", with: "") }

        XCTAssertTrue(testNames.isNotEmpty, "Expected to find ViewTest classes")

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
            .filter { $0.classes().contains(where: { $0.name.hasSuffix("ViewTest") }) }

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

    // MARK: - Naming

    func testViewTestClassesAreFinal() {
        let viewTests = testScope.classes()
            .withNameEndingWith("ViewTest")

        XCTAssertTrue(viewTests.isNotEmpty, "Expected to find ViewTest classes")

        let violators = viewTests.filter { !$0.modifiers.contains(.final) }

        XCTAssertTrue(
            violators.isEmpty,
            "ViewTest classes must be final:\n\(violators.map { $0.name }.joined(separator: "\n"))"
        )
    }

    func testViewTestClassesExtendXCTestCase() {
        let viewTests = testScope.classes()
            .withNameEndingWith("ViewTest")

        XCTAssertTrue(viewTests.isNotEmpty, "Expected to find ViewTest classes")

        viewTests.assertTrue(
            message: "ViewTest classes must extend XCTestCase"
        ) { cls in
            cls.inheritanceTypesNames.contains("XCTestCase")
        }
    }

    // MARK: - Hygiene

    func testTestCodeDoesNotContainPrintStatements() {
        let testSources = testScope.sources()
            .filter { $0.classes().contains(where: { $0.name.hasSuffix("ViewTest") || $0.name.hasSuffix("ViewRobot") || $0.name.hasSuffix("StateRobot") }) }

        let violators = testSources.filter { $0.source.contains("print(") }

        XCTAssertTrue(
            violators.isEmpty,
            "Test code must not contain print statements:\n\(violators.map { $0.fileName ?? "unknown" }.joined(separator: "\n"))"
        )
    }

    func testTestClassesEndWithTest() {
        let testClasses = testScope.classes()
            .filter { $0.inheritanceTypesNames.contains("XCTestCase") }

        testClasses.assertTrue(
            message: "Test classes extending XCTestCase must end with 'Test'"
        ) { cls in
            cls.name.hasSuffix("Test")
        }
    }
}
