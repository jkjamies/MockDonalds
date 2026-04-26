/// Marks a SwiftUI `View` as the renderer for a Circuit screen + state pair.
///
/// The attribute is a marker — it emits no code at the call site. The
/// `CircuitFactoryRegistryPlugin` build tool plugin scans for occurrences and
/// generates `GeneratedCircuitFactories.swift`, which the iosApp target
/// compiles in alongside hand-written code.
///
/// Annotated views must expose an `init(state:)` taking the declared state type
/// — the generated registry calls `ViewName(state: $0)` directly.
///
/// Views wrapped in `#if DEBUG` are automatically detected and gated in the
/// generated registry; no explicit flag is needed.
@attached(peer)
public macro CircuitInject(
    _ screen: Any.Type,
    _ state: Any.Type
) = #externalMacro(
    module: "CircuitMacrosPlugin",
    type: "CircuitInjectMacro"
)
