plugins {
    id("com.github.phantazmnetwork.gradle.plugin.phantazm-minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    api("com.github.steanky:ethylene-core:0.3.0")
}

tasks.jar {
    dependsOn(copyLibs)
}

val copyLibs = tasks.getByName<Copy>("copyLibs")