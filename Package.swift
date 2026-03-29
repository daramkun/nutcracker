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
            url: "https://github.com/daramkun/nutcracker/releases/download/v0.0.0/Nutcracker.xcframework.zip",
            checksum: "0000000000000000000000000000000000000000000000000000000000000000"
        )
    ]
)
