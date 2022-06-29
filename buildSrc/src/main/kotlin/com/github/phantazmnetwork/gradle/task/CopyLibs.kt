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
    //this should always be an absolute path
    var libraryDirectory : File? = null
        @Internal get
        set(value) {
            if (value != null) {
                field = if(value.isAbsolute) value else File(project.rootDir, value.path)
            }
            else field = null
        }

    //use @Input on String property instead of @DirectoryInput on DirectoryProperty so Gradle doesn't re-run the task
    //every time a file inside our folder changes
    val libraryDirectoryPath : String?
        @Optional @Input get() = libraryDirectory?.path

    var targetConfiguration : Configuration = project.configurations["runtimeClasspath"]
        @InputFiles get

    val artifactOutputs : FileCollection
        @OutputFiles get() = project.files(libraryDirectory?.let {
            getArtifacts(
                it, targetConfiguration.resolvedConfiguration.resolvedArtifacts).map {
                it.second
            }
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

        libraryDirectory?.let {
            getArtifacts(it, resolvedArtifacts).forEach {
                val artifactLastModified = it.first.file.lastModified()
                val targetLastModified = it.second.lastModified()

                if(artifactLastModified != targetLastModified) {
                    logger.info("Copying artifact ${it.first.file} to ${it.second}.")
                    it.first.file.copyTo(it.second, true)
                    it.second.setLastModified(artifactLastModified)
                }
            }
        }

        libraryDirectory?.walkTopDown()?.filter {
            it.isFile
        }?.forEach {
            val relative = libraryDirectory?.let { it1 -> it.relativeTo(it1) }
            val relativeParent = relative?.parentFile

            if(relativeParent == null) {
                logger.info("Deleting $it because its artifact group cannot be determined.")
                it.delete()
            } else {
                val artifactFileGroup = relativeParent.toPath().joinToString(".")
                val artifactFileName = relative.nameWithoutExtension

                if(resolvedArtifacts.none { artifact ->
                        artifact.moduleVersion.id.group.equals(artifactFileGroup, true) &&
                                artifact.file.nameWithoutExtension.equals(artifactFileName, true)
                    }) {
                    logger.info("Deleting $it because it does not match any known artifacts.")
                    it.delete()
                }
            }
        }
    }
}