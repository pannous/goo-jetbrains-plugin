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
        
        try {
            return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        } catch (e: Exception) {
            throw ExecutionException(
                "Failed to start Goo process:\n" +
                "Command: ${commandLine.commandLineString}\n" +
                "Working directory: ${commandLine.workDirectory?.absolutePath}\n" +
                "Error: ${e.message}\n" +
                "\n" +
                "Make sure:\n" +
                "1. The Goo binary is properly installed\n" +
                "2. The .goo file exists and is accessible\n" +
                "3. You have proper permissions to execute the binary",
                e
            )
        }
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
            
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            // Check if this is standard Go (which won't work with .goo files)
            if (output.contains("go version go") && !output.contains("goo")) {
                throw ExecutionException(
                    "Standard Go binary detected, but Goo files require Goo compiler!\n" +
                    "Binary: $binaryPath\n" +
                    "This is the standard Go compiler which cannot run .goo files.\n" +
                    "\n" +
                    "Solutions:\n" +
                    "1. Install the Goo compiler and ensure 'goo' is in PATH\n" +
                    "2. Place a Goo binary in your project's bin/ directory\n" +
                    "3. Set GOROOT to point to Goo installation (not standard Go)\n" +
                    "\n" +
                    "Current binary version: $output"
                )
            }
            
            if (exitCode != 0) {
                throw ExecutionException(
                    "Goo binary validation failed (exit code: $exitCode)\n" +
                    "Binary: $binaryPath\n" +
                    "Output: $output\n" +
                    "This may indicate the binary is in development or broken state.\n" +
                    "Common issues:\n" +
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
        
        // Ensure GOROOT is set if we can determine it
        if (commandLine.environment["GOROOT"].isNullOrEmpty()) {
            val goroot = System.getenv("GOROOT")
            if (!goroot.isNullOrEmpty()) {
                commandLine.environment["GOROOT"] = goroot
            } else {
                // Try to infer GOROOT from the binary path
                val binaryFile = File(gooBinaryPath)
                if (binaryFile.name == "go" && binaryFile.parent.endsWith("bin")) {
                    val inferredGoroot = binaryFile.parentFile.parent
                    commandLine.environment["GOROOT"] = inferredGoroot
                }
            }
        }
        
        return commandLine
    }

    private fun findGooBinary(): String {
        // 1. First priority: GOROOT/bin/go (configured Go SDK)
        val goroot = System.getenv("GOROOT")
        if (!goroot.isNullOrEmpty()) {
            val gorootBinary = File(goroot, "bin/go")
            if (gorootBinary.exists() && gorootBinary.canExecute()) {
                return gorootBinary.absolutePath
            }
        }

        // 2. Second priority: project's bin directory  
        val projectBin = File(configuration.project.basePath, "bin/goo")
        if (projectBin.exists() && projectBin.canExecute()) {
            return projectBin.absolutePath
        }

        // 3. Third priority: project's bin/go (development binary)
        val localGoo = File(configuration.project.basePath, "bin/go")
        if (localGoo.exists() && localGoo.canExecute()) {
            return localGoo.absolutePath
        }

        // 4. Fourth priority: system PATH for 'go' binary
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val goBinary = File(pathDir, "go")
            if (goBinary.exists() && goBinary.canExecute()) {
                return goBinary.absolutePath
            }
        }

        // 5. Last resort: system PATH for 'goo' binary
        for (pathDir in pathDirs) {
            val gooBinary = File(pathDir, "goo")
            if (gooBinary.exists() && gooBinary.canExecute()) {
                return gooBinary.absolutePath
            }
        }

        throw ExecutionException("Go/Goo binary not found. Please ensure:\n" +
            "1. GOROOT environment variable is set to your Go installation\n" +
            "2. Go is installed and available in PATH\n" +
            "3. Or place a goo/go binary in your project's bin/ directory")
    }
    
}