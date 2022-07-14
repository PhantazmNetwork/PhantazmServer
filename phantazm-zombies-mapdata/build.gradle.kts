plugins {
    id("phantazm.java-library-conventions")
}

repositories {
    mavenCentral()
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(projects.phantazmCommons)
}
