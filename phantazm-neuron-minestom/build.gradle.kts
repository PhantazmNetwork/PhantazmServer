plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(project(":phantazm-api"))
    api(project(":phantazm-neuron"))
}