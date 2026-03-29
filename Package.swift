// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Nutcracker",
    platforms: [
        .iOS(.v13),
        .macOS(.v10_15)
    ],
    products: [
        .library(
            name: "Nutcracker",
            targets: ["Nutcracker"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "Nutcracker",
            url: "https://github.com/daramkun/nutcracker/releases/download/v0.1.4/Nutcracker.xcframework.zip",
            checksum: "5af8f0e5af277bd040bc0f70b705448bef7ad4812daa3322e30f47d07e9fc306"
        )
    ]
)
