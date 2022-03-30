plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(project(":phantazm-commons"))
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
    testImplementation(libs.ethylene.toml)
}