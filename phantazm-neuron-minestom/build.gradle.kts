plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(projects.phantazmApi)
    api(projects.phantazmNeuron)
}