package com.github.phantazmnetwork.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import java.io.File

abstract class CopyLibs : DefaultTask() {
    //@Internal prevents Gradle from treating this property as an input and tracking its changes
    var libraryDirectory : File = project.buildDir
        @Internal get
        set(value) {
            field = if(value.isAbsolute) value else File(project.rootDir, value.path)
        }

    //use @Input on String property instead of @DirectoryInput on DirectoryProperty so Gradle doesn't re-run the task
    //every time a file inside our folder changes
    val libraryDirectoryPath : String
        @Input get() = libraryDirectory.path

    var targetConfiguration : Configuration = project.configurations["runtimeClasspath"]
        @InputFiles get

    val artifactOutputs : FileCollection
        @OutputFiles get() = project.files(getArtifacts(libraryDirectory, targetConfiguration.resolvedConfiguration
            .resolvedArtifacts).map {
                it.second
            })

    private fun getArtifacts(base: File, artifacts: Iterable<ResolvedArtifact>) :
            Iterable<Pair<ResolvedArtifact, File>> {
        return artifacts.map {
            var target = base
            for(dir in it.moduleVersion.id.group.split('.')) {
                target = target.resolve(dir)
            }

            it to target.resolve(it.file.name)
        }
    }

    @TaskAction
    fun copyLibs() {
        val resolvedArtifacts = targetConfiguration.resolvedConfiguration.resolvedArtifacts

        getArtifacts(libraryDirectory, resolvedArtifacts).forEach {
            if(!it.second.exists()) {
                logger.info("Creating $it")
                it.first.file.copyTo(it.second, false)
            }
        }

        libraryDirectory.walkTopDown().filter {
            it.isFile
        }.forEach {
            val relative = it.relativeTo(libraryDirectory)
            val relativeParent = relative.parentFile

            val artifactFileGroup = relativeParent.toPath().joinToString(".")
            val artifactFileName = relative.nameWithoutExtension

            if(resolvedArtifacts.none { artifact ->
                    artifact.moduleVersion.id.group == artifactFileGroup &&
                            artifactFileName == artifact.file.nameWithoutExtension
                }) {
                logger.info("Deleting $it.")
                it.delete()
            }
        }
    }
}