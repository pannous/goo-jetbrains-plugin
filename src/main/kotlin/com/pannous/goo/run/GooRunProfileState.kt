package com.pannous.goo.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

class GooRunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: GooRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        return ColoredProcessHandler(commandLine)
    }

    private fun createCommandLine(): GeneralCommandLine {
        val gooBinaryPath = findGooBinary()
        val commandLine = GeneralCommandLine()
        
        commandLine.exePath = gooBinaryPath
        commandLine.addParameter("run")
        commandLine.addParameter(configuration.filePath)
        
        val workingDir = configuration.workingDirectory.ifEmpty { 
            File(configuration.filePath).parent 
        }
        commandLine.workDirectory = File(workingDir)
        
        return commandLine
    }

    private fun findGooBinary(): String {
        // First try to find goo binary in the plugin's bundled resources
        val pluginDir = System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .find { it.contains("goo-intellij") }
            ?.let { File(it).parentFile }
        
        pluginDir?.let { dir ->
            val bundledBinary = File(dir, "bin/goo")
            if (bundledBinary.exists() && bundledBinary.canExecute()) {
                return bundledBinary.absolutePath
            }
        }

        // Try project's bin directory
        val projectBin = File(configuration.project.basePath, "bin/goo")
        if (projectBin.exists() && projectBin.canExecute()) {
            return projectBin.absolutePath
        }

        // Try system PATH
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val gooBinary = File(pathDir, "goo")
            if (gooBinary.exists() && gooBinary.canExecute()) {
                return gooBinary.absolutePath
            }
        }

        throw ExecutionException("Goo binary not found. Please ensure goo is installed and available in PATH or bundled with the plugin.")
    }
}