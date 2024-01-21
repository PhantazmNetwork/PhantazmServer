plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(projects.phantazmStats)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.caffeine)
    implementation(libs.commons)
    implementation(libs.ethylene.core)
    implementation(libs.ethylene.mapper)
    implementation(libs.element.core)
    implementation(libs.toolkit.collection)
    implementation(libs.toolkit.function)
    implementation(libs.vector.core)
}