plugins {
    java
}

allprojects {
    group = "org.phantazm"
    version = "1.0-SNAPSHOT"
    description = "Data for the Zombies minigame"
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/ethylene/maven/")
        }
        filter {
            includeModuleByRegex("com\\.github\\.steanky", "ethylene-.+")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/vector/maven/")
        }
        filter {
            includeModule("com.github.steanky", "vector-core")
        }
    }
}

dependencies {
    implementation("com.github.steanky:ethylene-core:0.23.0")
    implementation("com.github.steanky:ethylene-mapper:0.23.0")
    implementation("com.github.steanky:vector-core:0.9.2")
    implementation("net.kyori:adventure-api:4.11.0")
}