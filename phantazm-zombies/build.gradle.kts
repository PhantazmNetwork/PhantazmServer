plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(projects.phantazmApi)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmZombiesMapdata)
}
