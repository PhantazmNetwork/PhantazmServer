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

fun Configuration.exclude(provider: Provider<MinimalExternalModuleDependency>) {
    val module = provider.get().module
    exclude(module.group, module.name)
}

val includeTransitive: Configuration by configurations.creating {
    exclude(libs.fabric.loader)
    exclude(libs.fastutil)
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

    implementation(projects.phantazmCommons)
    implementation(projects.phantazmZombiesMapdata)
    implementation(libs.ethylene.yaml)

    @Suppress("UnstableApiUsage")
    includeTransitive(libs.renderer)
    includeTransitive(projects.phantazmCommons)
    includeTransitive(projects.phantazmZombiesMapdata)
    @Suppress("UnstableApiUsage")
    includeTransitive(libs.ethylene.yaml)

    val resolutionResult = includeTransitive.incoming.resolutionResult
    resolutionResult.allComponents {
        when (val idCopy = id) {
            resolutionResult.root.id -> {
                return@allComponents
            }
            is ModuleComponentIdentifier -> {
                include(idCopy.group, idCopy.module, idCopy.version)
            }
            is ProjectComponentIdentifier -> {
                include(project(idCopy.projectPath))
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
            "${it}_${base.archivesName.get()}"
        }
    }
}