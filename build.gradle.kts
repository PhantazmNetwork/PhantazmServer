plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.phantazmnetwork"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("com.github.Minestom:Minestom:9165a4d2b3")
    implementation("com.github.steanky:ethylene-toml:0.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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
