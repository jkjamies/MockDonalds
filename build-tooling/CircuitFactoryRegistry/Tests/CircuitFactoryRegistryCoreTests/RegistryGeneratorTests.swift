import XCTest
@testable import CircuitFactoryRegistryCore

final class RegistryGeneratorTests: XCTestCase {

    func test_extractsEntryFromAnnotatedView() {
        let source = """
        import SwiftUI

        @CircuitInject(HomeScreen.self, HomeUiState.self)
        struct HomeView: View {
            let state: HomeUiState
            var body: some View { EmptyView() }
        }
        """
        let entries = RegistryGenerator.entries(in: source)
        XCTAssertEqual(entries, [
            CircuitInjectEntry(
                viewType: "HomeView",
                screenType: "HomeScreen",
                stateType: "HomeUiState",
                isDebugOnly: false
            ),
        ])
    }

    func test_marksEntryDebugOnlyWhenWrappedInIfDebug() {
        let source = """
        #if DEBUG
        @CircuitInject(DebugMenuScreen.self, DebugMenuUiState.self)
        struct DebugMenuView: View {
            let state: DebugMenuUiState
            var body: some View { EmptyView() }
        }
        #endif
        """
        let entries = RegistryGenerator.entries(in: source)
        XCTAssertEqual(entries.first?.isDebugOnly, true)
    }

    func test_ignoresStructsWithoutAttribute() {
        let source = """
        struct UnrelatedView: View {
            var body: some View { EmptyView() }
        }
        """
        XCTAssertTrue(RegistryGenerator.entries(in: source).isEmpty)
    }

    func test_ignoresAttributesWithUnsupportedArguments() {
        // Plain identifiers (not `.self`) should be ignored — the macro
        // signature requires type metadata, but we want the parser tolerant
        // of malformed sources rather than crashing the build.
        let source = """
        @CircuitInject(HomeScreen, HomeUiState)
        struct HomeView: View { var body: some View { EmptyView() } }
        """
        XCTAssertTrue(RegistryGenerator.entries(in: source).isEmpty)
    }

    func test_emitterPlacesDebugEntriesInsideIfDebugBlock() {
        let entries = [
            CircuitInjectEntry(
                viewType: "HomeView",
                screenType: "HomeScreen",
                stateType: "HomeUiState",
                isDebugOnly: false
            ),
            CircuitInjectEntry(
                viewType: "DebugMenuView",
                screenType: "DebugMenuScreen",
                stateType: "DebugMenuUiState",
                isDebugOnly: true
            ),
        ]
        let output = RegistryEmitter.emit(entries: entries)

        XCTAssertTrue(output.contains("ScreenUiFactory<HomeScreen, HomeUiState> { HomeView(state: $0) }"))
        XCTAssertTrue(output.contains("#if DEBUG"))
        XCTAssertTrue(output.contains("factories.append(ScreenUiFactory<DebugMenuScreen, DebugMenuUiState> { DebugMenuView(state: $0) })"))
        XCTAssertTrue(output.contains("#endif"))

        // Debug append must come after the non-debug array literal closes.
        let debugRange = output.range(of: "#if DEBUG")
        let homeRange = output.range(of: "HomeView(state: $0)")
        XCTAssertNotNil(debugRange)
        XCTAssertNotNil(homeRange)
        XCTAssertLessThan(homeRange!.lowerBound, debugRange!.lowerBound)
    }

    func test_emitterProducesEmptyArrayWhenNoEntries() {
        let output = RegistryEmitter.emit(entries: [])
        XCTAssertTrue(output.contains("var factories: [UiFactory] = ["))
        XCTAssertTrue(output.contains("return factories"))
        XCTAssertFalse(output.contains("#if DEBUG"))
    }
}
