package com.pannous.goo.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File

class GooRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    var filePath: String = ""
    var workingDirectory: String = ""

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return GooRunConfigurationEditor()
    }

    override fun checkConfiguration() {
        if (filePath.isEmpty()) {
            throw RuntimeConfigurationError("File path is not specified")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : RunProfileState {
            override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): com.intellij.execution.ExecutionResult? {
                val gooBinaryPath = findGooBinary()
                val commandLine = GeneralCommandLine()
                
                commandLine.exePath = gooBinaryPath
                commandLine.addParameter("run")
                commandLine.addParameter(filePath)
                
                val workingDir = workingDirectory.ifEmpty { 
                    File(filePath).parent 
                }
                commandLine.workDirectory = File(workingDir)
                
                // Add environment variables including GOROOT
                commandLine.environment.putAll(System.getenv())
                ensureGOROOT(commandLine, gooBinaryPath)
                
                try {
                    val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                    return com.intellij.execution.DefaultExecutionResult(null as com.intellij.execution.ui.ExecutionConsole?, processHandler)
                } catch (e: Exception) {
                    throw RuntimeConfigurationError(
                        "Failed to start Goo process:\n" +
                        "Command: ${commandLine.commandLineString}\n" +
                        "Error: ${e.message}\n" +
                        "\nMake sure the Goo binary is properly installed."
                    )
                }
            }
        }
    }
    
    private fun findGooBinary(): String {
        // 1. GOROOT/bin/go (configured Go SDK)
        val goroot = System.getenv("GOROOT")
        if (!goroot.isNullOrEmpty()) {
            val gorootBinary = File(goroot, "bin/go")
            if (gorootBinary.exists() && gorootBinary.canExecute()) {
                return gorootBinary.absolutePath
            }
        }

        // 2. Project bin/go
        val localGoo = File(project.basePath, "bin/go")
        if (localGoo.exists() && localGoo.canExecute()) {
            return localGoo.absolutePath
        }

        // 3. System PATH
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val goBinary = File(pathDir, "go")
            if (goBinary.exists() && goBinary.canExecute()) {
                return goBinary.absolutePath
            }
        }

        throw RuntimeConfigurationError(
            "Go/Goo binary not found. Please ensure:\n" +
            "1. GOROOT environment variable is set\n" +
            "2. Go is installed and available in PATH\n" +
            "3. Or place a go binary in your project's bin/ directory"
        )
    }
    
    private fun ensureGOROOT(commandLine: GeneralCommandLine, binaryPath: String) {
        if (commandLine.environment["GOROOT"].isNullOrEmpty()) {
            val goroot = System.getenv("GOROOT")
            if (!goroot.isNullOrEmpty()) {
                commandLine.environment["GOROOT"] = goroot
            } else {
                // Try to infer GOROOT from binary path
                val binaryFile = File(binaryPath)
                if (binaryFile.name == "go" && binaryFile.parent.endsWith("bin")) {
                    val inferredGoroot = binaryFile.parentFile.parent
                    commandLine.environment["GOROOT"] = inferredGoroot
                }
            }
        }
    }
}