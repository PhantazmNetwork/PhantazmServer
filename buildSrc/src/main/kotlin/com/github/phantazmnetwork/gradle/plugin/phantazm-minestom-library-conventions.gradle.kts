package com.github.phantazmnetwork.gradle.plugin

plugins {
    id("com.github.phantazmnetwork.gradle.plugin.phantazm-java-library-conventions")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    api("com.github.Minestom:Minestom:5efa6d7980")
}