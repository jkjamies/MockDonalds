import SwiftCompilerPlugin
import SwiftSyntaxMacros

@main
struct CircuitMacrosPlugin: CompilerPlugin {
    let providingMacros: [Macro.Type] = [
        CircuitInjectMacro.self,
    ]
}
