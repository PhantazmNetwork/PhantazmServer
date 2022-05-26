plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    api(project(":phantazm-neuron-minestom"))
    implementation(project(":phantazm-commons"))
    implementation(libs.ethylene.core)
    implementation(libs.miniMessage)
}