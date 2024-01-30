plugins {
    id("phantazm.java-library-conventions")
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