plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(projects.phantazmCommons)
    implementation(projects.phantazmLoader)
    implementation(libs.toolkit.function)
    implementation(libs.toolkit.collection)
    api(libs.ethylene.mapper)
}
