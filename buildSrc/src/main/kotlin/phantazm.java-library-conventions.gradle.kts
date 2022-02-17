plugins {
    `java-library`
    id("phantazm.java-conventions")
}

tasks.jar {
    dependsOn(copyLibs)
}

val copyLibs = tasks.getByName<Copy>("copyLibs")
