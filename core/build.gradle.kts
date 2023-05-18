plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    api(projects.phantazmCommons)
    api(libs.ethylene.core)
    api(libs.ethylene.mapper)
    api(libs.commons.lang3)
    api(libs.caffeine)
    api(libs.element.core)
    api(libs.toolkit.collection)
    api(libs.toolkit.function)
    api(libs.vector.core)
}
