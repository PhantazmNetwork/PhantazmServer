import java.security.MessageDigest

plugins {
    id("phantazm.java-conventions")
}

repositories {
    maven("https://jitpack.io")
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":phantazm-api"))
    implementation(project(":phantazm-zombies"))

    implementation(libs.miniMessage)
    implementation(libs.ethylene.toml)
}

tasks.jar {
    dependsOn("copyLibs")

    archiveBaseName.set("server")
    archiveVersion.set("")
    archiveClassifier.set("")

    doFirst {
        manifest {
            val copyLibsTask = tasks.getByName("copyLibs")
            val libsFolder = copyLibsTask.extensions["libsFolder"] as File

            @Suppress("UNCHECKED_CAST")
            val outputFiles = copyLibsTask.extensions["outputFiles"] as List<File>

            attributes(
                "Class-Path" to outputFiles.joinToString(" ") {
                    "libs/${it.relativeTo(libsFolder).path.replace('\\', '/')}"
                },
                "Main-Class" to "com.github.phantazmnetwork.server.Main",
                "Multi-Release" to true
            )
        }
    }
}

tasks.register("setupServer") {
    dependsOn(tasks.jar)

    fun sha1(file: File) : String {
        val md = MessageDigest.getInstance("SHA-1")
        file.forEachBlock(4096) { bytes, size ->
            md.update(bytes, 0, size)
        }

        return md.digest().joinToString("") {
            "%02x".format(it)
        }
    }

    doFirst {
        val newFile = tasks.jar.get().archiveFile.get().asFile
        val oldFile = File("$rootDir/run/server-1/server.jar")

        if(oldFile.exists()) {
            val oldHash = sha1(oldFile)
            val newHash = sha1(newFile)

            if(oldHash != newHash) {
                println("Contents of server file have changed; overwriting the old file.")
                println("Old hash: $oldHash")
                println("New hash: $newHash")

                oldFile.delete()
                newFile.copyTo(oldFile, true)
            }
            else {
                println("No changes detected in server file.")
            }
        }
        else {
            println("Copying the server file.")
            newFile.copyTo(oldFile)
        }
    }
}
