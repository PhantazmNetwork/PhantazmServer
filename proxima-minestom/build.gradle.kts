plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(projects.phantazmCore)

    implementation(libs.commons)
    implementation(libs.element.core)
    implementation(libs.ethylene.core)
    implementation(libs.ethylene.mapper)
    implementation(libs.proxima.core)
}
