import XCTest

/// Startup performance tests — measure app launch time to detect regressions.
/// Uses XCTMetric with baselines for regression detection.
final class StartupPerformanceTest: XCTestCase {

    func testColdStartup() throws {
        let app = XCUIApplication()

        measure(metrics: [XCTApplicationLaunchMetric()]) {
            app.launch()
        }
    }

    func testColdStartupToFirstScreen() throws {
        let app = XCUIApplication()

        measure(metrics: [XCTApplicationLaunchMetric(waitUntilResponsive: true)]) {
            app.launch()

            // Wait until the home screen content appears
            let homeElement = app.descendants(matching: .any)["HomeUserName"]
            XCTAssertTrue(homeElement.waitForExistence(timeout: 10))
        }
    }
}
