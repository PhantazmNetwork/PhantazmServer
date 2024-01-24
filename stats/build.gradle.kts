plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    implementation(libs.adventure.key)
    implementation(libs.caffeine)
    implementation(libs.commons)
    implementation(libs.fastutil)
    implementation(libs.hikariCP)
    implementation(libs.toolkit.function)
}
