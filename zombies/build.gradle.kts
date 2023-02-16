plugins {
    id("phantazm.minestom-library-conventions")
    id("com.github.steanky.element-autodoc") version "0.1.0"
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmZombiesMapdata)
}

elementAutodoc {
    projectDescription.set("Test description")
    projectUrl.set("https://www.phantazm.org")
    founded.set(1676253017L)
    maintainers.set(listOf("Steank"))
}

tasks.named("elementAutodoc") {
    (this as SourceTask).source = java.sourceSets["main"].allJava
}
