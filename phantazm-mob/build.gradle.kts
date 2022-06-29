plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    api(project(":phantazm-neuron-minestom"))
    implementation(project(":phantazm-api"))
    implementation(project(":phantazm-commons"))
    implementation(libs.adventure.text.minimessage)
    testImplementation(libs.ethylene.yaml)
}
