plugins {
    id("com.gradleup.shadow") version "9.2.2" apply false
    kotlin("jvm") version "2.4.21" apply false
}

allprojects {
    group = "net.mc26experiment"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}
