package com.pannous.goo.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

class GooRunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: GooRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
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
        
        // Add environment variables
        commandLine.environment.putAll(System.getenv())
        
        return commandLine
    }

    private fun findGooBinary(): String {
        // 1. First priority: bundled goo binary in plugin resources
        val bundledBinary = findBundledGooBinary()
        if (bundledBinary != null) {
            return bundledBinary
        }

        // 2. Second priority: project's bin directory  
        val projectBin = File(configuration.project.basePath, "bin/goo")
        if (projectBin.exists() && projectBin.canExecute()) {
            return projectBin.absolutePath
        }

        // 3. Third priority: local goo directory (our renamed binary)
        val localGoo = File(configuration.project.basePath, "bin/go")
        if (localGoo.exists() && localGoo.canExecute()) {
            return localGoo.absolutePath
        }

        // 4. Fourth priority: current directory goo binary
        val currentDirGoo = File("./goo")
        if (currentDirGoo.exists() && currentDirGoo.canExecute()) {
            return currentDirGoo.absolutePath
        }

        // 5. Last resort: system PATH
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val gooBinary = File(pathDir, "goo")
            if (gooBinary.exists() && gooBinary.canExecute()) {
                return gooBinary.absolutePath
            }
        }

        throw ExecutionException("Goo binary not found. Please ensure goo is installed and available in PATH or bundled with the plugin.")
    }
    
    private fun findBundledGooBinary(): String? {
        // Try multiple methods to find the bundled binary
        val possibleLocations = listOf(
            // Method 1: Look for plugin JAR in classpath
            System.getProperty("java.class.path")
                .split(File.pathSeparator)
                .find { it.contains("goo") && it.endsWith(".jar") }
                ?.let { File(it).parentFile }
                ?.let { File(it, "goo") },
                
            // Method 2: Look relative to plugin directory
            System.getProperty("java.class.path")
                .split(File.pathSeparator)
                .find { it.contains("plugins") && it.contains("goo") }
                ?.let { File(it).parentFile }
                ?.let { File(it, "goo") },
                
            // Method 3: Look in common plugin locations
            File("/opt/other/goo-intellij/goo"),
            File("./goo"),
            File("../goo")
        )
        
        return possibleLocations
            .filterNotNull()
            .find { it.exists() && it.canExecute() }
            ?.absolutePath
    }
}