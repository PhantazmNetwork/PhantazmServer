plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    api(projects.phantazmCommons)
    api(libs.ethylene.core)
    api(libs.caffeine)
    api(libs.element.core)
    api(libs.toolkit.collection)
    api(libs.toolkit.function)
}
