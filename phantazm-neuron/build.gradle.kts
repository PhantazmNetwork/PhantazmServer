plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(project(":phantazm-commons"))
    implementation(libs.caffeine)
    testImplementation(libs.ethylene.toml)
}