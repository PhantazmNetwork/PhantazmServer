rootProject.name = "phantazm"
include("phantazm-api", "phantazm-server", "phantazm-zombies", "phantazm-commons", "phantazm-neuron",
    "phantazm-neuron-minestom", "phantazm-zombies-mapeditor", "phantazm-zombies-mapdata")

val localSettings = file("local.settings.gradle.kts")
if(localSettings.exists()) {
    //apply from local settings too, if it exists
    //can be used to sideload Minestom for faster testing
    apply(localSettings)
}

//necessary for phantazm-zombies-mapeditor module which contains a Fabric mod
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}
