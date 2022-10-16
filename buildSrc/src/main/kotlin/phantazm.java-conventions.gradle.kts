import com.github.phantazmnetwork.gradle.task.CopyLibs
import com.github.phantazmnetwork.gradle.task.SetupServer

plugins {
    java
}

group = "com.github.phantazmnetwork"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
        }
        filter {
            includeModuleByRegex("com\\.github\\.steanky", "ethylene-.+")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steank-f1g/element-QiJ/maven/")
        }
        filter {
            includeModule("com.github.steanky", "element-core")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steank-f1g/toolkit/maven/")
        }
        filter {
            includeModuleByRegex("com\\.github\\.steanky", "toolkit-.+")
        }
    }
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
pluginManager.withPlugin("java") {
    val libs = catalogs.named("libs")

    dependencies.addProvider("compileOnly", libs.findLibrary("jetbrains.annotations").get())
    dependencies.addProvider("testCompileOnly", libs.findLibrary("jetbrains.annotations").get())

    dependencies.addProvider("testImplementation", libs.findLibrary("junit.jupiter.api").get())
    dependencies.addProvider("testImplementation", libs.findLibrary("mockito.junit.jupiter").get())

    dependencies.addProvider("testRuntimeOnly", libs.findLibrary("junit.jupiter.engine").get())
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).tags(
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Requirements:",
        "implNote:a:Implementation Note:"
    )
}

tasks.register<CopyLibs>("copyLibs")
tasks.register<SetupServer>("setupServer")
