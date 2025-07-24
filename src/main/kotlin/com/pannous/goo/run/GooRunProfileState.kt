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
        
        // Validate the binary before attempting to run
        validateGooBinary(commandLine.exePath)
        
        return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
    }
    
    private fun validateGooBinary(binaryPath: String) {
        val binaryFile = File(binaryPath)
        if (!binaryFile.exists()) {
            throw ExecutionException("Goo binary not found at: $binaryPath")
        }
        if (!binaryFile.canExecute()) {
            throw ExecutionException("Goo binary is not executable: $binaryPath")
        }
        
        // Quick validation - try to run with version command
        try {
            val process = ProcessBuilder(binaryPath, "version")
                .redirectErrorStream(true)
                .start()
            
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val output = process.inputStream.bufferedReader().readText()
                throw ExecutionException(
                    "Goo binary validation failed (exit code: $exitCode)\n" +
                    "Binary: $binaryPath\n" +
                    "Output: $output\n" +
                    "This may indicate the binary is in development or broken state.\n" +
                    "Common issues:\n" +
                    "- Binary may be standard Go instead of Goo\n" +
                    "- Binary may be incomplete/corrupted\n" +
                    "- Missing required Go toolchain components"
                )
            }
        } catch (e: Exception) {
            if (e is ExecutionException) throw e
            throw ExecutionException("Failed to validate goo binary at $binaryPath: ${e.message}")
        }
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
        // Try to find the bundled binary in plugin resources
        try {
            // First try to find via class loader resource
            val resourceUrl = this.javaClass.classLoader.getResource("bin/goo")
            if (resourceUrl != null) {
                // If it's a jar: URL, we need to extract it
                if (resourceUrl.protocol == "jar") {
                    // Extract the binary to a temp location
                    val inputStream = resourceUrl.openStream()
                    val tempFile = File.createTempFile("goo-binary", "")
                    tempFile.deleteOnExit()
                    tempFile.setExecutable(true)
                    
                    inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    return tempFile.absolutePath
                } else {
                    // It's a file: URL, we can use it directly
                    return File(resourceUrl.toURI()).absolutePath
                }
            }
        } catch (e: Exception) {
            // Fall back to other methods if resource loading fails
        }
        
        // Fallback methods for development/testing
        val possibleLocations = listOf(
            // Look in build output
            File("build/resources/main/bin/goo"),
            File("src/main/resources/bin/goo"),
            // Development location
            File("/opt/other/goo-intellij/goo"),
            File("./goo"),
            File("../goo")
        )
        
        return possibleLocations
            .find { it.exists() && it.canExecute() }
            ?.absolutePath
    }
}