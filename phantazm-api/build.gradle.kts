plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    api(project(":phantazm-commons"))
    api(libs.ethylene.core)
    api(libs.caffeine)
}
