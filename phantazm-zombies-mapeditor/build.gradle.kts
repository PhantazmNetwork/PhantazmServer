plugins {
    id("phantazm.java-library-conventions")

    // this IntelliJ error is a false-positive, just ignore it:
    // https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
    @Suppress("UnstableApiUsage")
    alias(libs.plugins.fabric.loom)
}

base {
    archivesName.set("phantazm-zombies-mapeditor")
}

repositories {
    maven("https://jitpack.io")
    maven("https://server.bbkr.space/artifactory/libs-release")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mappings) {
        artifact {
            classifier = "v2"
        }
    }
    modImplementation(libs.bundles.fabric)
    modImplementation(libs.renderer)
    modImplementation(libs.libgui)

    implementation(project(":phantazm-commons"))
    implementation(project(":phantazm-zombies-mapdata"))

    implementation(libs.ethylene.toml)
}

tasks.processResources {
    inputs.property("version", project.version)
}

tasks.compileJava {
    options.release.set(java.toolchain.languageVersion.get().asInt())
}

tasks.jar {
    from("../LICENSE") {
        rename {
            "${it}_${base.archivesName}"
        }
    }
}