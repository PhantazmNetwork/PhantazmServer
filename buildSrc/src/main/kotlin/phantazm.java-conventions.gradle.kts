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

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register("copyLibs") {
    val outputFiles = mutableListOf<File>()
    extensions.add("outputFiles", outputFiles)

    outputs.upToDateWhen {
        for(project in rootProject.subprojects) {
            if(project.tasks.classes.get().didWork) {
                return@upToDateWhen false
            }
        }

        true
    }

    doFirst {
        val libsFolder = extensions["libsFolder"] as File

        val resolvedArtifacts = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
        resolvedArtifacts.forEach { artifact ->
            val dirs = artifact.moduleVersion.id.group.split('.')

            var target = libsFolder
            for(dir in dirs) {
                target = target.resolve(dir)
            }
            target.mkdirs()
            target = target.resolve(artifact.file.name)

            val absolute = File(rootDir, target.path)
            if(!absolute.exists()) {
                println("Creating $absolute")
                artifact.file.copyTo(absolute, false)
            }

            outputFiles.add(target.relativeTo(libsFolder))
        }

        val absolute = File(rootDir, libsFolder.path)
        absolute.walkTopDown().filter {
            it.isFile
        }.forEach {
            val relative = it.relativeTo(absolute)
            val parent = relative.parentFile

            val groupName = parent.toPath().joinToString(".")
            val artifactFileName = relative.nameWithoutExtension

            var matchingArtifact : ResolvedArtifact? = null
            for(artifact in resolvedArtifacts) {
                if(artifact.moduleVersion.id.group == groupName && artifactFileName
                        .startsWith("${artifact.moduleVersion.id.name}-")) {
                    val artifactVersion = artifact.moduleVersion.id.version

                    if(artifactFileName.endsWith("-$artifactVersion")) {
                        matchingArtifact = artifact
                    }
                    else {
                        println("Detected version change for ${artifact.moduleVersion.id.module}, is now " +
                                "$artifactVersion. The old version will be deleted.")
                    }

                    break
                }
            }

            if(matchingArtifact == null) {
                println("Deleting $it.")
                it.delete()
            }
        }
    }
}
