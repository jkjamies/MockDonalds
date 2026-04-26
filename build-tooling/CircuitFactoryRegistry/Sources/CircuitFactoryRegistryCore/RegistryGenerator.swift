import Foundation
import SwiftParser
import SwiftSyntax

/// Public entry point for the registry build tool.
///
/// Tests construct entries directly via `RegistryEmitter`; production callers
/// (the SPM build plugin's executable) feed in source paths and let this
/// function do parse + walk + emit in one pass.
public enum RegistryGenerator {
    public static func entries(in source: String) -> [CircuitInjectEntry] {
        let tree = Parser.parse(source: source)
        let visitor = CircuitInjectVisitor()
        visitor.walk(tree)
        return visitor.entries
    }

    public static func generate(sourcePaths: [String]) -> String {
        var entries: [CircuitInjectEntry] = []
        for path in sourcePaths {
            guard let source = try? String(contentsOfFile: path, encoding: .utf8) else { continue }
            entries.append(contentsOf: Self.entries(in: source))
        }
        return RegistryEmitter.emit(entries: entries)
    }
}
