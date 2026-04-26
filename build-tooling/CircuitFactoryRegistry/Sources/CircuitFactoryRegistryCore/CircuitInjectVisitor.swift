import Foundation
import SwiftSyntax

public struct CircuitInjectEntry: Equatable {
    public let viewType: String
    public let screenType: String
    public let stateType: String
    public let isDebugOnly: Bool

    public init(viewType: String, screenType: String, stateType: String, isDebugOnly: Bool) {
        self.viewType = viewType
        self.screenType = screenType
        self.stateType = stateType
        self.isDebugOnly = isDebugOnly
    }
}

/// Walks a parsed Swift source tree and collects every `@CircuitInject`
/// attribute attached to a struct/class/actor declaration.
///
/// Tracks `#if DEBUG` nesting so generated entries can be gated correctly —
/// authors don't need an explicit `debug:` argument on the macro.
public final class CircuitInjectVisitor: SyntaxVisitor {
    public private(set) var entries: [CircuitInjectEntry] = []
    private var debugDepth = 0

    public override init(viewMode: SyntaxTreeViewMode = .sourceAccurate) {
        super.init(viewMode: viewMode)
    }

    public override func visit(_ node: IfConfigDeclSyntax) -> SyntaxVisitorContinueKind {
        for clause in node.clauses {
            let isDebug = isDebugCondition(clause.condition)
            if isDebug { debugDepth += 1 }
            if let elements = clause.elements {
                walk(Syntax(elements))
            }
            if isDebug { debugDepth -= 1 }
        }
        return .skipChildren
    }

    public override func visit(_ node: StructDeclSyntax) -> SyntaxVisitorContinueKind {
        collect(typeName: node.name.text, attributes: node.attributes)
        return .visitChildren
    }

    public override func visit(_ node: ClassDeclSyntax) -> SyntaxVisitorContinueKind {
        collect(typeName: node.name.text, attributes: node.attributes)
        return .visitChildren
    }

    public override func visit(_ node: ActorDeclSyntax) -> SyntaxVisitorContinueKind {
        collect(typeName: node.name.text, attributes: node.attributes)
        return .visitChildren
    }

    private func collect(typeName: String, attributes: AttributeListSyntax) {
        for element in attributes {
            guard let attribute = element.as(AttributeSyntax.self) else { continue }
            guard attributeName(attribute) == "CircuitInject" else { continue }
            guard let (screen, state) = parseArguments(attribute) else { continue }
            entries.append(
                CircuitInjectEntry(
                    viewType: typeName,
                    screenType: screen,
                    stateType: state,
                    isDebugOnly: debugDepth > 0
                )
            )
        }
    }

    private func attributeName(_ attribute: AttributeSyntax) -> String? {
        attribute.attributeName.as(IdentifierTypeSyntax.self)?.name.text
    }

    private func parseArguments(_ attribute: AttributeSyntax) -> (String, String)? {
        guard case let .argumentList(args) = attribute.arguments else { return nil }
        let typeNames = args.compactMap { typeName(from: $0.expression) }
        guard typeNames.count == 2 else { return nil }
        return (typeNames[0], typeNames[1])
    }

    /// Extracts the type name from `HomeScreen.self` style expressions.
    private func typeName(from expression: ExprSyntax) -> String? {
        guard
            let memberAccess = expression.as(MemberAccessExprSyntax.self),
            memberAccess.declName.baseName.text == "self",
            let base = memberAccess.base?.as(DeclReferenceExprSyntax.self)
        else { return nil }
        return base.baseName.text
    }

    private func isDebugCondition(_ condition: ExprSyntax?) -> Bool {
        guard let condition else { return false }
        if let identifier = condition.as(DeclReferenceExprSyntax.self) {
            return identifier.baseName.text == "DEBUG"
        }
        return false
    }
}
