plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
    maven("https://dl.cloudsmith.io/public/steank-f1g/element-QiJ/maven/")
}

dependencies {
    implementation(projects.phantazmCore)
    implementation(projects.phantazmMob)
    implementation(projects.phantazmZombiesMapdata)
    implementation(libs.element.core)
}
