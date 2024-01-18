import org.phantazm.gradle.task.CopyLibs

plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmZombiesMapdata)
    implementation(projects.phantazmZombies)
    implementation(projects.phantazmLoader)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.ethylene.toml)
    implementation(libs.toml)
    implementation(libs.ethylene.yaml)
    implementation(libs.yaml)
    implementation(libs.ethylene.json)
    implementation(libs.gson)
    implementation(libs.element.core)
    implementation(libs.ethylene.mapper)

    implementation(libs.reflections)

    runtimeOnly(libs.mariadb)
    runtimeOnly(libs.sqlite)
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
                "Main-Class" to "org.phantazm.server.PhantazmServer",
                "Multi-Release" to true
        )
    }
}

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    into("$rootDir/run/server-1/")
}