import com.github.phantazmnetwork.gradle.task.CopyLibs
import org.gradle.kotlin.dsl.support.unzipTo
import java.net.URL
import java.nio.channels.Channels

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

tasks.getByName<CopyLibs>("copyLibs") {
    libraryDirectory = File("/run/server-1/libs")
}

tasks.jar {
    val copyLibsTask = tasks.getByName<CopyLibs>("copyLibs")
    dependsOn(copyLibsTask)

    archiveBaseName.set("server")
    archiveVersion.set("")
    archiveClassifier.set("")

    manifest {
        attributes(
            "Class-Path" to copyLibsTask.outputs.files.joinToString(" ") {
                "libs/${it.relativeTo(copyLibsTask.libraryDirectory).toPath().joinToString("/")}"
            },
            "Main-Class" to "com.github.phantazmnetwork.server.Main",
            "Multi-Release" to true
        )
    }
}

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    into("$rootDir/run/server-1/")
}

tasks.register("setupServer") {
    doFirst {
        val rootDir = rootProject.rootDir
        val runFolder = rootDir.resolve("run")
        val forceSetup = project.hasProperty("forceSetup")
        val serverData = rootDir.resolve("run.zip")

        if(runFolder.exists() && runFolder.listFiles()?.size == 0) {
            if(!forceSetup) {
                logger.lifecycle("No action was taken because the run folder already exists. To force the setup task " +
                        "to run, use -PforceSetup.")
                return@doFirst
            }

            logger.lifecycle("You are forcing server setup. This will delete EVERYTHING in the run folder. Type \"Y\"" +
                    " without quotation marks (case-sensitive) and hit enter if you wish to proceed; otherwise, type " +
                    "any other character to cancel.")
            if(readLine() != "Y") {
                logger.lifecycle("Cancelled server setup; no files have been changed.")
                return@doFirst
            }

            if(!runFolder.deleteRecursively()) {
                logger.error("Failed to delete some files. They might be in use by another program. Setup cancelled.")
                return@doFirst
            }
        }

        runFolder.mkdir()
        unzipTo(runFolder, serverData)

        fun downloadUrlTo(url: URL, file: File) {
            file.parentFile.mkdirs()

            Channels.newChannel(url.openStream()).use { byteChannel ->
                file.outputStream().channel.use { fileChannel ->
                    fileChannel.transferFrom(byteChannel, 0, Long.MAX_VALUE)
                }
            }
        }

        if(!serverData.exists()) {
            logger.error("There is no run.zip folder in the root project directory.")
            return@doFirst
        }

        val downloads = listOf(
            URL("https://papermc.io/api/v2/projects/velocity/versions/3.1.1/builds/98/downloads/velocity-3.1.1-" +
                    "98.jar")
                    to File("/velocity/velocity.jar"),
            URL("https://ci.viaversion.com/job/ViaBackwards/lastSuccessfulBuild/artifact/build/libs/ViaBackwards" +
                    "-4.2.0-SNAPSHOT.jar")
                    to File("/velocity/plugins/ViaBackwards-4.2.0-SNAPSHOT.jar"),
            URL("https://ci.viaversion.com/job/ViaRewind/lastSuccessfulBuild/artifact/all/target/ViaRewind-2.0.3" +
                    "-SNAPSHOT.jar")
                    to File("/velocity/plugins/ViaRewind-2.0.3.jar"),
            URL("https://ci.viaversion.com/job/ViaVersion/lastBuild/artifact/build/libs/ViaVersion-4.2.0-" +
                    "SNAPSHOT.jar")
                    to File("/velocity/plugins/ViaVersion-4.2.0-SNAPSHOT.jar")
        )

        logger.lifecycle("Downloading Velocity and necessary plugins.")
        downloads.forEach {
            downloadUrlTo(it.first, File(runFolder, it.second.path))
        }

        logger.lifecycle("Setup complete.")
    }
}