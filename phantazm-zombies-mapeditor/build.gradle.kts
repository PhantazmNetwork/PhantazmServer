import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
@Suppress(
    "DSL_SCOPE_VIOLATION",
    "MISSING_DEPENDENCY_CLASS",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
    "FUNCTION_CALL_EXPECTED"
)
plugins {
    id("phantazm.java-library-conventions")

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.shadow)
}

base {
    archivesName.set("phantazm-zombies-mapeditor")
}

repositories {
    maven("https://jitpack.io")
    maven("https://server.bbkr.space/artifactory/libs-release")
}

val shade: Configuration by configurations.creating {
    val fastutilModule = libs.fastutil.get().module
    exclude(fastutilModule.group, fastutilModule.name)
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

    shade(projects.phantazmCommons)
    shade(projects.phantazmZombiesMapdata)

    @Suppress("UnstableApiUsage")
    shade(libs.ethylene.yaml)

    implementation(shade)
}

tasks.processResources {
    inputs.property("version", project.version)
}

tasks.compileJava {
    options.release.set(java.toolchain.languageVersion.get().asInt())
}

val relocateShadowJar by tasks.creating(ConfigureShadowRelocation::class) {
    target = tasks.shadowJar.get()
    prefix = "com.github.phantazmnetwork.zombies.mapeditor.client.shadow"
}

tasks.shadowJar {
    dependsOn(relocateShadowJar)
    archiveClassifier.set("shadowJar")
    configurations = listOf(shade)

    from("../LICENSE") {
        rename {
            "${it}_${base.archivesName.get()}"
        }
    }
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
}