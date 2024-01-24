package org.phantazm.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getByName
import java.io.File
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import javax.inject.Inject
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

//only exists because of Gradle limitations
abstract class InsertManifest : DefaultTask() {
    @get:Inject
    abstract val taskContainer: TaskContainer

    @get:Input
    abstract var attributes: Map<String, Any>

    @get:Input
    abstract var rootFolder: String

    @get:InputDirectory
    abstract var libraryDirectory: File

    var jarFile: File? = null
        @OutputFile get
        set(value) {
            if (value != null) {
                field = if (value.isAbsolute) value else File(project.rootDir, value.path)
            } else field = null
        }

    @TaskAction
    fun insertManifest() {
        val copyLibs = taskContainer.getByName<CopyLibs>("copyLibs")

        val manifest = Manifest()
        val attributes = manifest.mainAttributes
        val files = copyLibs.outputs.files

        val classPath = files.joinToString(" ") {
            "$rootFolder/${it.relativeTo(libraryDirectory)}"
        }

        attributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        attributes[Attributes.Name.CLASS_PATH] = classPath

        for (attribute in this.attributes) {
            attributes[Attributes.Name(attribute.key)] = attribute.value.toString()
        }

        val tempFile = kotlin.io.path.createTempFile("phantazm")
        JarFile(jarFile!!).use { input ->
            JarOutputStream(tempFile.outputStream(), manifest).use { output ->
                val entryIterator = input.entries().asIterator()

                var count = 0
                while (entryIterator.hasNext()) {
                    val entry = entryIterator.next()
                    if (count <= 1) {
                        count++
                        continue
                    }

                    output.putNextEntry(entry)
                    input.getInputStream(entry).transferTo(output)
                }
            }
        }

        tempFile.copyTo(jarFile!!.toPath(), true)
        tempFile.deleteIfExists()
    }
}
