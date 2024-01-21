import org.phantazm.gradle.task.CopyLibs

plugins {
    java
}

allprojects {
    this.group = "org.phantazm"
    this.version = "1.0-SNAPSHOT"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/ethylene/maven/")
        }
        filter {
            includeModuleByRegex("com\\.github\\.steanky", "ethylene-.+")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/element/maven/")
        }
        filter {
            includeModule("com.github.steanky", "element-core")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/vector/maven/")
        }
        filter {
            includeModule("com.github.steanky", "vector-core")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/toolkit/maven/")
        }
        filter {
            includeModuleByRegex("com\\.github\\.steanky", "toolkit-.+")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/steanky/proxima/maven/")
        }
        filter {
            includeModule("com.github.steanky", "proxima-core")
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