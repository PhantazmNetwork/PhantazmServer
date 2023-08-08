plugins {
    id("phantazm.java-library-conventions")
}

dependencies {
    api(libs.adventure.key)
    api(libs.jooq)
    api(libs.hikariCP)
    implementation(libs.toolkit.function)
}
