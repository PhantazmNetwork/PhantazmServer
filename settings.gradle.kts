rootProject.name = "phantazm"

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) {
    //apply from local settings too, if it exists
    apply(localSettings)
}

includeBuild("./minestom")
includeBuild("./commons")

pluginManagement {
    repositories {
        //necessary for phantazm-zombies-mapeditor module which contains a Fabric mod
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }

        maven("https://dl.cloudsmith.io/public/steanky/element/maven/")
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val toSkip = gradle.startParameter.projectProperties.getOrDefault("skipBuild", "").split(",")

sequenceOf(
        "core",
        "mob",
        "proxima-minestom",
        "server",
        "stats",
        "velocity",
        "zombies",
        "snbt-builder",
        "dev-launcher"
).forEach {
    if (!toSkip.contains(it)) {
        include(":phantazm-$it")
        project(":phantazm-$it").projectDir = file(it)
        println("Building module $it")
    } else {
        println("Ignoring module $it")
    }
}