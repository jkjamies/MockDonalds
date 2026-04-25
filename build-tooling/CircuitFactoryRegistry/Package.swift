// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "CircuitFactoryRegistry",
    platforms: [.macOS(.v14)],
    products: [
        .executable(
            name: "CircuitFactoryRegistry",
            targets: ["CircuitFactoryRegistry"]
        ),
    ],
    dependencies: [
        .package(
            url: "https://github.com/swiftlang/swift-syntax.git",
            from: "600.0.0"
        ),
    ],
    targets: [
        .target(
            name: "CircuitFactoryRegistryCore",
            dependencies: [
                .product(name: "SwiftSyntax", package: "swift-syntax"),
                .product(name: "SwiftParser", package: "swift-syntax"),
            ]
        ),
        .executableTarget(
            name: "CircuitFactoryRegistry",
            dependencies: ["CircuitFactoryRegistryCore"]
        ),
        .testTarget(
            name: "CircuitFactoryRegistryCoreTests",
            dependencies: ["CircuitFactoryRegistryCore"]
        ),
    ]
)
