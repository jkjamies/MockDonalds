// swift-tools-version: 6.0
import CompilerPluginSupport
import PackageDescription

let package = Package(
    name: "CircuitMacros",
    platforms: [.macOS(.v14), .iOS(.v16)],
    products: [
        .library(
            name: "CircuitMacros",
            targets: ["CircuitMacros"]
        ),
    ],
    dependencies: [
        .package(
            url: "https://github.com/swiftlang/swift-syntax.git",
            from: "600.0.0"
        ),
    ],
    targets: [
        .macro(
            name: "CircuitMacrosPlugin",
            dependencies: [
                .product(name: "SwiftSyntax", package: "swift-syntax"),
                .product(name: "SwiftSyntaxMacros", package: "swift-syntax"),
                .product(name: "SwiftCompilerPlugin", package: "swift-syntax"),
            ]
        ),
        .target(
            name: "CircuitMacros",
            dependencies: ["CircuitMacrosPlugin"]
        ),
    ]
)
