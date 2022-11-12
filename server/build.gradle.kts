import com.github.phantazmnetwork.gradle.task.CopyLibs
import com.github.phantazmnetwork.gradle.task.SetupServer

plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmMessaging)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmNeuronMinestom)
    implementation(projects.phantazmZombiesMapdata)
    implementation(projects.phantazmZombies)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.ethylene.toml)
    implementation(libs.ethylene.yaml)
    implementation(libs.element.core)
}

tasks.getByName<CopyLibs>("copyLibs") {
    libraryDirectory = File("run/server-1/libs")
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
                "libs/${it.relativeTo(copyLibsTask.libraryDirectory!!).toPath().joinToString("/")}"
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
