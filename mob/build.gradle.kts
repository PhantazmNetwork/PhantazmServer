plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    api(projects.phantazmNeuronMinestom)
    implementation(projects.phantazmCore)
    implementation(projects.phantazmCommons)
    implementation(libs.adventure.text.minimessage)
    testImplementation(libs.ethylene.core)
}