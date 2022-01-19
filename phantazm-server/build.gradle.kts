plugins {
    id("com.github.phantazmnetwork.gradle.plugin.phantazm-java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":phantazm-api"))
    implementation(project(":phantazm-zombies"))

    implementation("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT")
}

tasks.shadowJar {
    manifest {
        attributes(
            "Main-Class" to "com.github.phantazmnetwork.server.Main",
            "Multi-Release" to true
        )
    }

    archiveBaseName.set("server")
    archiveVersion.set("")
    archiveClassifier.set("")

    finalizedBy(copyShadowJar)
}

val copyShadowJar = tasks.register<Copy>("copyShadowJar") {
    from(tasks.shadowJar)
    into("$rootDir/run/server-1/")
}

tasks.jar.get().enabled = false
