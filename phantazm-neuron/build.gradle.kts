plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(project(":phantazm-commons"))
    testImplementation(libs.ethylene.toml)
}