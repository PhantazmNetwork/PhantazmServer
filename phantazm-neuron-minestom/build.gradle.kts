plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(project(":phantazm-commons-minestom"))
    api(project(":phantazm-neuron"))
}