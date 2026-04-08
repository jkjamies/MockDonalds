// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "ArchitectureCheck",
    platforms: [.iOS(.v16), .macOS(.v13)],
    products: [
        .library(
            name: "ArchitectureCheck",
            targets: ["ArchitectureCheck"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/perrystreetsoftware/Harmonize.git", from: "0.9.0"),
    ],
    targets: [
        .target(
            name: "ArchitectureCheck"
        ),
        .testTarget(
            name: "HarmonizeTests",
            dependencies: [
                "ArchitectureCheck",
                .product(name: "Harmonize", package: "Harmonize"),
            ]
        ),
    ]
)
