rootProject.name = "phantazm"
include("phantazm-api", "phantazm-server", "phantazm-zombies", "phantazm-commons", "phantazm-neuron",
    "phantazm-neuron-minestom", "phantazm-zombies-mapeditor")

val localSettings = file("local.settings.gradle.kts")
if(localSettings.exists()) {
    //apply from local settings too, if it exists
    //can be used to sideload Minestom for faster testing
    apply(localSettings)
}

//add settings from phantazm-zombies-mapeditor too
apply("./phantazm-zombies-mapeditor/settings.gradle.kts")
include("phantazm-zombies-mapdata")
