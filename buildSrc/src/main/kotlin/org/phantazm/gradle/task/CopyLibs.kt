package org.phantazm.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import java.io.Serializable
import javax.inject.Inject

abstract class CopyLibs : DefaultTask() {
    @get:Inject
    abstract val objectFactory: ObjectFactory

    //@Internal prevents Gradle from treating this property as an input and tracking its changes
    //this should always be an absolute path
    var libraryDirectory: File? = null
        @Internal get
        set(value) {
            if (value != null) {
                field = if (value.isAbsolute) value else File(project.rootDir, value.path)
            } else field = null
        }

    //use @Input on String property instead of @DirectoryInput on DirectoryProperty so Gradle doesn't re-run the task
    //every time a file inside our folder changes
    val libraryDirectoryPath: String?
        @Optional @Input get() = libraryDirectory?.path

    data class ArtifactEntry(val inputFile: File, val outputFile: File, val group: String) : Serializable

    @get:Internal
    val artifacts: SetProperty<ArtifactEntry> = objectFactory.setProperty()

    val artifactInputs: Provider<FileCollection>
        @InputFiles get() = artifacts.map {
            objectFactory.fileCollection().from(it.map { entry -> entry.inputFile })
        }

    val artifactOutputs: Provider<FileCollection>
        @OutputFiles get() = artifacts.map {
            objectFactory.fileCollection().from(it.map { entry -> entry.outputFile })
        }

    @TaskAction
    fun copyLibs() {
        val encountered = mutableSetOf<Pair<String, String>>()
        artifacts.get().forEach { entry ->
            val artifactLastModified = entry.inputFile.lastModified()
            val targetLastModified = entry.outputFile.lastModified()

            if (targetLastModified == 0L || artifactLastModified != targetLastModified) {
                logger.info("Copying artifact ${entry.inputFile} to ${entry.outputFile}.")
                entry.inputFile.copyTo(entry.outputFile, true)
                entry.outputFile.setLastModified(artifactLastModified)
            } else {
                logger.info("Skipping ${entry.inputFile} as it is not more recent than its target")
            }

            encountered.add(Pair(entry.group.lowercase(), entry.inputFile.nameWithoutExtension.lowercase()))
        }

        libraryDirectory?.walkTopDown()?.forEach {
            if (it.isDirectory) {
                val files = it.listFiles()
                if (files != null && files.isEmpty()) {
                    it.delete()
                }
            } else {
                val relative = libraryDirectory?.let { it1 -> it.relativeTo(it1) }
                val relativeParent = relative?.parentFile

                if (relativeParent == null) {
                    logger.info("Deleting $it because its artifact group cannot be determined.")
                    it.delete()
                } else {
                    val artifactFileGroup = relativeParent.toPath().joinToString(".").lowercase()
                    val artifactFileName = relative.nameWithoutExtension.lowercase()

                    if (!encountered.contains(Pair(artifactFileGroup, artifactFileName))) {
                        logger.info("Deleting $it because it does not match any known artifacts.")
                        it.delete()
                    }
                }
            }
        }
    }
}
