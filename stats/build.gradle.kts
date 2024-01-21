plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    api(libs.adventure.key)
    api(libs.hikariCP)
    api(libs.fastutil)
    implementation(libs.commons)
    implementation(libs.toolkit.function)
    implementation(libs.caffeine)
}
