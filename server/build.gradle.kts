import org.phantazm.gradle.task.CopyLibs
import org.phantazm.gradle.task.InsertManifest

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

val copyLibs = tasks.getByName<CopyLibs>("copyLibs") {
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
    archiveBaseName.set("server")
    archiveVersion.set("")
    archiveClassifier.set("")
}

val copyJar = tasks.register<Copy>("copyJar") {
    from(tasks.jar)
    into("$rootDir/run/server-1/")

    finalizedBy("insertManifest")
}

val insertManifest = tasks.getByName<InsertManifest>("insertManifest") {
    dependsOn(copyLibs)
    dependsOn(copyJar)

    this.attributes = mapOf("Main-Class" to "org.phantazm.server.PhantazmServer", "Multi-Release" to true)
    this.rootFolder = "libs"
    this.libraryDirectory = copyLibs.libraryDirectory!!
    this.jarFile = File("./run/server-1/server.jar")
}