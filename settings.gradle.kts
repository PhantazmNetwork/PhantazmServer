rootProject.name = "phantazm"

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) {
    //apply from local settings too, if it exists
    //can be used to sideload Minestom for faster testing
    apply(localSettings)
}

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

sequenceOf(
    "core",
    "commons",
    "messaging",
    "mob",
    "proxima-minestom",
    "server",
    "velocity",
    "zombies",
    "zombies-mapdata",
    //"zombies-mapeditor" //disable to speed up compilation (also if Loom decides to randomly error)
).forEach {
    include(":phantazm-$it")
    project(":phantazm-$it").projectDir = file(it)
}
