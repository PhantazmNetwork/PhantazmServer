package com.github.phantazmnetwork.gradle.plugin

plugins {
    java
}

group = "com.github.phantazmnetwork"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into("$rootDir/run/server-1/libs")
}