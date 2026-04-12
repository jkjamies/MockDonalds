import Testing

// Placeholder — exists so the `UnitTests` test plan has something to run
// until real pure-logic Swift unit tests land. Most shared business logic
// lives in KMP Kotlin and is covered by Kotest on `testAndroidHostTest`;
// Swift-side pure-logic tests will live here as iOS-only code grows
// (e.g. NavigationStateManager helpers, format helpers).
struct PlaceholderUnitTest {
    @Test func arithmeticSanity() {
        #expect(1 + 1 == 2)
    }
}
