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

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    compileOnly("org.jetbrains:annotations:22.0.0")
    testCompileOnly("org.jetbrains:annotations:22.0.0")
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
}