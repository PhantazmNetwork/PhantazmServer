// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
@Suppress(
    "DSL_SCOPE_VIOLATION",
    "MISSING_DEPENDENCY_CLASS",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
    "FUNCTION_CALL_EXPECTED"
)
plugins {
    id("phantazm.java-library-conventions")

    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

val shade: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(shade)

fun ModuleDependency.exclude(provider: Provider<MinimalExternalModuleDependency>) {
    val module = provider.get().module
    exclude(module.group, module.name)
}

fun DependencyHandlerScope.addShade(dependency: ProjectDependency) {
    shade(dependency) {
        exclude(libs.fastutil)
        exclude(libs.netty.buffer)
    }
}

dependencies {
    annotationProcessor(libs.velocity.api)

    implementation(libs.netty.buffer)
    implementation(libs.velocity.api)

    addShade(projects.phantazmCommons)
    addShade(projects.phantazmMessaging)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shade)
}

tasks.jar.get().enabled = false

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    into("$rootDir/run/velocity/plugins")
}
