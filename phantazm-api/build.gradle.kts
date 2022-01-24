plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    api("com.github.steanky:ethylene-core:0.4.2")
}

tasks.jar {
    dependsOn(copyLibs)
}

val copyLibs = tasks.getByName<Copy>("copyLibs")
