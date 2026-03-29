import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvmToolchain(17)

    jvm {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }

    val xcf = XCFramework("Nutcracker")

    iosX64 {
        binaries.framework {
            baseName = "Nutcracker"
            xcf.add(this)
        }
    }
    iosArm64 {
        binaries.framework {
            baseName = "Nutcracker"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Nutcracker"
            xcf.add(this)
        }
    }

    macosX64 {
        binaries.framework {
            baseName = "Nutcracker"
            xcf.add(this)
        }
    }
    macosArm64 {
        binaries.framework {
            baseName = "Nutcracker"
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            groupId = "com.daram"
            artifactId = "nutcracker"
            version = project.version.toString()

            afterEvaluate {
                from(components["kotlin"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/daramkun/nutcracker")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
