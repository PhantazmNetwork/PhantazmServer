// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("phantazm.java-conventions")

    alias(libs.plugins.fabric.loom)
}

version = "0.2.1+1.20.1"

base {
    archivesName.set("zombies-autosplits")
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft(libs.minecraft.oneTwenty)
    mappings(libs.yarn.mappings.oneTwenty) {
        artifact {
            classifier = "v2"
        }
    }

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api.oneTwenty)
    modImplementation(libs.modmenu)
    modImplementation(libs.cloth.config)

    implementation(projects.phantazmCommons)
    implementation(projects.phantazmMessaging)
    implementation(libs.ethylene.yaml)
    implementation(libs.yaml)

    include(projects.phantazmCommons)
    include(projects.phantazmMessaging)
    include(libs.adventure.key)
    include(libs.examination.api)
    include(libs.examination.string)
    include(libs.ethylene.core)
    include(libs.ethylene.yaml)
    include(libs.toolkit.collection)
    include(libs.toolkit.function)
    include(libs.snakeyaml)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.jar {
    from("../LICENSE") {
        rename { "${it}_${archiveBaseName.get()}" }
    }
}
