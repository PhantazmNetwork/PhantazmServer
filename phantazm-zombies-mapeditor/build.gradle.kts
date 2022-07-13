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
}

base {
    archivesName.set("phantazm-zombies-mapeditor")
}

repositories {
    maven("https://jitpack.io")
    maven("https://server.bbkr.space/artifactory/libs-release")
}

val transitiveInclude: Configuration by configurations.creating {
    exclude("it.unimi.dsi", "fastutil")
    exclude("net.fabricmc", "fabric-loader")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mappings) {
        artifact {
            classifier = "v2"
        }
    }
    modImplementation(libs.bundles.fabric)
    modImplementation(libs.libgui)

    modImplementation(libs.renderer)
    @Suppress("UnstableApiUsage")
    transitiveInclude(libs.renderer)

    implementation(project(":phantazm-commons"))
    transitiveInclude(project(":phantazm-commons"))
    implementation(project(":phantazm-zombies-mapdata"))
    transitiveInclude(project(":phantazm-zombies-mapdata"))

    implementation(libs.ethylene.yaml)
    @Suppress("UnstableApiUsage")
    transitiveInclude(libs.ethylene.yaml)
}

project.afterEvaluate {
    transitiveInclude.incoming.resolutionResult.allComponents {
        val idCopy = id
        dependencies {
            when (idCopy) {
                is ModuleComponentIdentifier -> {
                    include(idCopy.group, idCopy.module, idCopy.version)
                }
                is ProjectComponentIdentifier -> {
                    if (idCopy.projectPath != project.path) {
                        include(project(idCopy.projectPath))
                    }
                }
            }
        }
    }
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