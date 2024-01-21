plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmProximaMinestom)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.commons)
    implementation(libs.element.core)
    implementation(libs.ethylene.core)
    implementation(libs.ethylene.mapper)
    implementation(libs.proxima.core)
    implementation(libs.toolkit.collection)
}
