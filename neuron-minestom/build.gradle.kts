plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(projects.phantazmCore)
    api(projects.phantazmNeuron)
}
