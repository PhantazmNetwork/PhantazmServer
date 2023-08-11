plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(projects.phantazmCommons)
    api(libs.ethylene.mapper)
}
