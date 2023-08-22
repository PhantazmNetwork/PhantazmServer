// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("phantazm.java-conventions")

    alias(libs.plugins.fabric.loom)
}

version = "1.4.0+1.19.4"

base {
    archivesName.set("phantazm-zombies-mapeditor")
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://jitpack.io")
        }
        filter {
            includeGroup("com.github.0x3C50")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.fabricmc.net")
        }
        filter {
            includeGroup("net.fabricmc")
        }
    }
    maven("https://server.bbkr.space/artifactory/libs-release")
}

dependencies {
    minecraft(libs.minecraft.oneNineteen)
    mappings(libs.yarn.mappings.oneNineteen) {
        artifact {
            classifier = "v2"
        }
    }

    modImplementation(libs.fabric.loader)
    modImplementation(libs.libgui)
    modImplementation(libs.fabric.api.oneNineteen)
    modImplementation(libs.renderer)

    implementation(projects.phantazmCommons)
    implementation(projects.phantazmMessaging)
    implementation(projects.phantazmZombiesMapdata)
    implementation(libs.ethylene.yaml)

    include(libs.libgui)
    include(libs.renderer)

    include(projects.phantazmCommons)
    include(projects.phantazmMessaging)
    include(projects.phantazmZombiesMapdata)
    include(libs.adventure.api)
    include(libs.adventure.key)
    include(libs.adventure.text.minimessage)
    include(libs.caffeine)
    include(libs.ethylene.core)
    include(libs.ethylene.yaml)
    include(libs.ethylene.mapper)
    include(libs.examination.api)
    include(libs.examination.string)
    include(libs.snakeyaml)
    include(libs.toolkit.collection)
    include(libs.toolkit.function)
    include(libs.vector.core)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.compileJava {
    options.release.set(java.toolchain.languageVersion.get().asInt())
}

tasks.jar {
    from("../LICENSE") {
        rename { "${it}_${archiveBaseName.get()}" }
    }
}
