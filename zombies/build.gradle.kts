plugins {
    id("phantazm.minestom-library-conventions")
    id("com.github.steanky.element-autodoc") version "0.1.2"
}

dependencies {
    api(projects.phantazmCore)
    api(projects.phantazmMessaging)
    api(projects.phantazmMob)
    api(projects.phantazmStats)
    api(projects.phantazmZombiesMapdata)

    implementation(projects.phantazmLoader)
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
