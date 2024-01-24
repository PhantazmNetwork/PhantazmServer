plugins {
    id("phantazm.minestom-library-conventions")
}

dependencies {
    implementation(libs.adventure.api)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.text.serializer.gson)
    implementation(libs.hephaistos.common)
    implementation(libs.hephaistos.gson)
}
