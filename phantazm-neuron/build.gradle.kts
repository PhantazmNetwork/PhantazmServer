plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(projects.phantazmCommons)
    implementation(libs.caffeine)
    testImplementation(libs.ethylene.toml)
}