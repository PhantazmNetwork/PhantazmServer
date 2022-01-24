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

    implementation("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT")
    implementation("com.github.steanky:ethylene-toml:0.4.2")
}

tasks.jar {
    dependsOn(copyLibs)

    manifest {
        attributes(
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { "libs/${it.name}" },
            "Main-Class" to "com.github.phantazmnetwork.server.Main",
            "Multi-Release" to true
        )
    }

    archiveBaseName.set("server")
    archiveVersion.set("")
    archiveClassifier.set("")

    finalizedBy(copyJar)
}

val copyLibs = tasks.getByName<Copy>("copyLibs")

val copyJar = tasks.register<Copy>("copyJar") {
    from(tasks.jar)
    into("$rootDir/run/server-1/")
}
