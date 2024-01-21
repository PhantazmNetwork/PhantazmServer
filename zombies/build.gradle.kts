plugins {
    id("phantazm.minestom-library-conventions")
    id("com.github.steanky.element-autodoc") version "0.1.2"
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmProximaMinestom)
    implementation(projects.phantazmStats)

    implementation(libs.adventure.api)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.commons)
    implementation(libs.element.core)
    implementation(libs.ethylene.core)
    implementation(libs.ethylene.mapper)
    implementation(libs.proxima.core)
    implementation(libs.toolkit.collection)
    implementation(libs.vector.core)
}

elementAutodoc {
    projectDescription.set("Phantazm's official 'Zombies' minigame")
    projectUrl.set("https://www.phantazm.org")
    founded.set(1642244496L)
    maintainers.set(listOf("Steank"))
}

tasks.named("elementAutodoc") {
    (this as SourceTask).source = java.sourceSets["main"].allJava
}
