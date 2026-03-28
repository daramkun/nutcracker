pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "nutcracker-demo"

// Composite Build: 로컬 nutcracker 소스를 직접 참조 (publishToMavenLocal 불필요)
includeBuild("../nutcracker")
