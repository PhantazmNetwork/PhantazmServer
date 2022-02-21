package com.github.phantazmnetwork.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SetupServer : DefaultTask() {
    private val files: MutableMap<String, File> = mutableMapOf()

    var dataFolder: File = project.rootDir.resolve("runData")
        @Internal get

    @TaskAction
    fun setupServer() {
        val runFolder = project.rootProject.rootDir.resolve("run")

        if(runFolder.exists() && !runFolder.listFiles().isNullOrEmpty()) {
            if(!project.hasProperty("forceSetup")) {
                logger.lifecycle("No action was taken because the run folder already exists. To force the setup task " +
                        "to run, use -PforceSetup.")
                return
            }

            logger.lifecycle("You are forcing server setup. This will delete EVERYTHING in the run folder. Type \"Y\"" +
                    " without quotation marks (case-sensitive) and hit enter if you wish to proceed; otherwise, type " +
                    "any other character to cancel.")
            if(readLine() != "Y") {
                logger.lifecycle("Cancelled server setup; no files have been changed.")
                return
            }

            if(!runFolder.deleteRecursively()) {
                logger.error("Failed to delete some files. They might be in use by another program. Setup cancelled.")
                return
            }
        }

        runFolder.mkdir()
        if(dataFolder.exists()) {
            logger.lifecycle("Copying server files to $runFolder")
            dataFolder.copyRecursively(runFolder, true)
        }

        logger.lifecycle("Server setup complete.")
    }
}