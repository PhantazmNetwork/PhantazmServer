plugins {
    id("com.github.phantazmnetwork.gradle.plugin.phantazm-minestom-library-conventions")
}

tasks.jar {
    dependsOn(copyLibs)
}

val copyLibs = tasks.getByName<Copy>("copyLibs")