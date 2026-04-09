import XCTest

/// Top-level robot for e2e journey tests. Provides common actions:
/// launch the app, navigate tabs, trigger deep links, wait for content.
///
/// Uses XCUITest for process-isolated element access since e2e tests
/// run in a separate process from the app (XCUITest target).
final class AppRobot {

    let app = XCUIApplication()

    private let launchTimeout: TimeInterval = 10
    private let elementTimeout: TimeInterval = 5

    // MARK: - Launch

    func launchApp() {
        app.launch()
    }

    func launchWithDeepLink(_ uri: String) {
        app.launchEnvironment["DEEP_LINK_URI"] = uri
        app.launch()
    }

    // MARK: - Tab Navigation

    func tapTab(_ label: String) {
        let tab = app.tabBars.buttons[label]
        XCTAssertTrue(tab.waitForExistence(timeout: elementTimeout), "Tab '\(label)' not found")
        tab.tap()
    }

    // MARK: - Element Assertions

    func assertElementDisplayed(_ testTag: String) {
        let element = app.descendants(matching: .any)[testTag]
        XCTAssertTrue(
            element.waitForExistence(timeout: elementTimeout),
            "Expected element with testTag '\(testTag)' to be displayed"
        )
    }

    func assertElementNotDisplayed(_ testTag: String) {
        let element = app.descendants(matching: .any)[testTag]
        XCTAssertFalse(
            element.waitForExistence(timeout: 2),
            "Expected element with testTag '\(testTag)' to NOT be displayed"
        )
    }

    // MARK: - Interaction

    func tapElement(_ testTag: String) {
        let element = app.descendants(matching: .any)[testTag]
        XCTAssertTrue(
            element.waitForExistence(timeout: elementTimeout),
            "Expected element with testTag '\(testTag)' to be displayed before tapping"
        )
        element.tap()
    }

    func typeText(_ testTag: String, text: String) {
        let element = app.descendants(matching: .any)[testTag]
        XCTAssertTrue(
            element.waitForExistence(timeout: elementTimeout),
            "Expected element with testTag '\(testTag)' to be displayed before typing"
        )
        element.tap()
        element.typeText(text)
    }
}
