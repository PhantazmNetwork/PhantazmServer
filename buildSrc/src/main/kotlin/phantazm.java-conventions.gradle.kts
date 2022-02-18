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
    val libs = File("/run/server-1/libs")

    doFirst {
        val outputFiles = mutableListOf<File>()
        for(artifact: ResolvedArtifact in configurations.runtimeClasspath.get().resolvedConfiguration
            .resolvedArtifacts) {
            val dirs = artifact.moduleVersion.id.group.split('.')

            var target = libs
            for(dir: String in dirs) {
                target = target.resolve(dir)
            }
            target.mkdirs()
            target = target.resolve(artifact.file.name)

            val absolute = File(rootDir, target.path)

            val artifactName = artifact.moduleVersion.id.name
            val artifactVersion = artifact.moduleVersion.id.version

            //delete different versions of the same artifact
            absolute.parentFile.listFiles { file ->
                val extensionlessName = file.nameWithoutExtension
                !extensionlessName.endsWith("-$artifactVersion") &&
                        extensionlessName.startsWith("$artifactName-") &&
                        file.extension == artifact.file.extension
            }?.forEach {
                val extensionlessName = it.nameWithoutExtension
                val split = extensionlessName.split(artifactName)

                if(split.size >= 2) {
                    val group = artifact.moduleVersion.id.group
                    val oldArtifactVersion = split[1].removePrefix("-")

                    println("Version mismatch: found $group:$artifactName:$oldArtifactVersion, replacing with " +
                            "$group:$artifactName:$artifactVersion")
                }

                println("Deleting $it")
                it.delete()
            }

            if(!absolute.exists()) {
                println("Creating $absolute")
                artifact.file.copyTo(absolute, false)
            }

            outputFiles.add(target)
        }

        extensions.add("outputFiles", outputFiles)
        extensions.add("libsFolder", libs)
    }

    val upToDateSpec = object : Spec<Task> {
        override fun isSatisfiedBy(element: Task?): Boolean {
            for(project : Project in rootProject.subprojects) {
                if(project.tasks.compileJava.get().state.didWork) {
                    return false
                }
            }

            return true
        }
    }

    outputs.upToDateWhen(upToDateSpec)
    extensions.add("upToDateSpec", upToDateSpec)
}
