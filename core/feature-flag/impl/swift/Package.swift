// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "FeatureFlagBridge",
    platforms: [.iOS(.v16)],
    products: [
        .library(
            name: "FeatureFlagBridge",
            targets: ["FeatureFlagBridge"]
        ),
    ],
    dependencies: [
        .package(
            url: "https://github.com/harness/ff-ios-client-sdk.git",
            from: "1.3.4"
        ),
    ],
    targets: [
        .target(
            name: "FeatureFlagBridge",
            dependencies: [
                .product(name: "ff-ios-client-sdk", package: "ff-ios-client-sdk"),
            ]
        ),
    ]
)
