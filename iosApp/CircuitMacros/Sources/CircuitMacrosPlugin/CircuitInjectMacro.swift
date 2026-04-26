import SwiftSyntax
import SwiftSyntaxMacros

/// `@CircuitInject` is a marker attribute. The build tool plugin (not this
/// macro) does the cross-file aggregation that produces the factory registry,
/// so the macro itself emits nothing.
public struct CircuitInjectMacro: PeerMacro {
    public static func expansion(
        of node: AttributeSyntax,
        providingPeersOf declaration: some DeclSyntaxProtocol,
        in context: some MacroExpansionContext
    ) throws -> [DeclSyntax] {
        return []
    }
}
