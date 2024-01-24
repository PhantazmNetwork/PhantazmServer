plugins {
    id("phantazm.minestom-library-conventions")
}

tasks.jar {
    manifest {
        attributes(
                "Main-Class" to "org.phantazm.devlauncher.Main"
        )
    }

    destinationDirectory = file("$rootDir")
    archiveFileName = "dev-launcher.jar"
}