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
    implementation(projects.phantazmProximaMinestom)
    implementation(projects.phantazmStats)
    implementation(projects.phantazmZombies)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.caffeine)
    implementation(libs.commons)
    implementation(libs.element.core)
    implementation(libs.ethylene.core)
    implementation(libs.ethylene.json)
    implementation(libs.ethylene.mapper)
    implementation(libs.ethylene.toml)
    implementation(libs.ethylene.yaml)
    implementation(libs.gson)
    implementation(libs.hikariCP)
    implementation(libs.proxima.core)
    implementation(libs.reflections)
    implementation(libs.toml)
    implementation(libs.toolkit.collection)
    implementation(libs.toolkit.function)
    implementation(libs.vector.core)
    implementation(libs.yaml)

    runtimeOnly(libs.mariadb)
    runtimeOnly(libs.sqlite)
}

tasks.getByName<CopyLibs>("copyLibs") {
    libraryDirectory = File("run/server-1/libs")

    artifacts.set(configurations.getByName("runtimeClasspath").incoming.artifacts.resolvedArtifacts.map {
        return@map it.mapNotNull {
            val component = it.id.componentIdentifier

            val groupString: String
            if (component is ModuleComponentIdentifier) {
                groupString = component.group
            } else if (component is ProjectComponentIdentifier) {
                groupString = "org.phantazm"
            } else {
                throw IllegalStateException("Unknown component type ${it.javaClass}")
            }

            val split = groupString.split('.')
            var target = libraryDirectory!!
            split.forEach {
                target = target.resolve(it)
            }

            target = target.resolve(it.file.name)

            return@mapNotNull CopyLibs.ArtifactEntry(it.file, target, groupString)
        }.toSet()
    })
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