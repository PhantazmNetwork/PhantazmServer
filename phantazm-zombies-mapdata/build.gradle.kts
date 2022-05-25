plugins {
    id("phantazm.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(project(":phantazm-commons"))
    api(libs.adventure.key)
}
