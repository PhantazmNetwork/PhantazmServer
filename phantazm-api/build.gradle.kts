plugins {
    id("com.github.phantazmnetwork.gradle.plugin.phantazm-java-conventions")
    `java-library`
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    api("com.github.steanky:ethylene-toml:0.3.0")
}
