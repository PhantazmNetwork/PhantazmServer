plugins {
    id("phantazm.minestom-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(project(":phantazm-api"))
    implementation(project(":phantazm-mob"))
    implementation(project(":phantazm-zombies-mapdata"))
    implementation(project(":phantazm-mob"))
}
