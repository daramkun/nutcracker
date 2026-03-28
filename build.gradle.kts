plugins {
    kotlin("jvm") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
}

allprojects {
    group = "com.daram"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
