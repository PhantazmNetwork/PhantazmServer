import com.github.phantazmnetwork.gradle.task.CopyLibs
import com.github.phantazmnetwork.gradle.task.SetupServer

plugins {
    id("phantazm.java-conventions")
}

repositories {
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":phantazm-api"))
    implementation(project(":phantazm-neuron-minestom"))
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
            "Main-Class" to "com.github.phantazmnetwork.server.PhantazmServer",
            "Multi-Release" to true
        )
    }
}

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    into("$rootDir/run/server-1/")
}

tasks.getByName<SetupServer>("setupServer") {
    dataFolder = rootProject.rootDir.resolve("defaultRunData")
}