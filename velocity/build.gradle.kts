// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-can-t-be-called-in-this-context-by-implicit-recei
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("phantazm.java-library-conventions")

    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
    }
}

val shade: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    annotationProcessor(libs.velocity.api)

    implementation(libs.commons)
    implementation(libs.velocity.api)

    shade(libs.commons)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shade)
}

tasks.jar {
    enabled = false
}

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    into("$rootDir/run/velocity/plugins")
}
