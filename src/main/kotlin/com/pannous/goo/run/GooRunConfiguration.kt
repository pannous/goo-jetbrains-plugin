package com.pannous.goo.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File
import javax.swing.JComponent
import javax.swing.JLabel

class GooRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    var scriptName = ""

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<GooRunConfiguration>() {
            override fun resetEditorFrom(s: GooRunConfiguration) {}
            override fun applyEditorTo(s: GooRunConfiguration) {}
            override fun createEditor(): JComponent = JLabel("Goo Run Configuration")
        }
    }

    override fun checkConfiguration() {
        // Basic validation
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : RunProfileState {
            override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): com.intellij.execution.ExecutionResult {
                val commandLine = GeneralCommandLine()
                
                // Try to find a working Go installation, avoiding broken ones
                val goPath = findWorkingGo()
                commandLine.exePath = goPath
                commandLine.addParameter("version")
                
                // Set proper GOROOT to avoid bootstrap issues
                commandLine.environment.putAll(System.getenv())
                commandLine.environment["GOROOT"] = getGOROOTForPath(goPath)
                
                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                return com.intellij.execution.DefaultExecutionResult(null, processHandler)
            }
        }
    }
    
    private fun findWorkingGo(): String {
        val candidates = listOf(
            "/opt/homebrew/bin/go",  // Homebrew Go (usually works)
            "/usr/local/bin/go",     // Manual install
            "/opt/other/go/bin/go"   // Your Goo (but might be broken)
        )
        
        for (path in candidates) {
            if (File(path).exists() && isWorkingGo(path)) {
                return path
            }
        }
        
        // Fallback to PATH
        return "go"
    }
    
    private fun isWorkingGo(path: String): Boolean {
        return try {
            val process = ProcessBuilder(path, "version").start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getGOROOTForPath(goPath: String): String {
        return when {
            goPath.contains("/opt/homebrew/") -> "/opt/homebrew/Cellar/go/1.24.5/libexec"
            goPath.contains("/usr/local/") -> "/usr/local/go"
            goPath.contains("/opt/other/go/") -> "/opt/other/go"
            else -> System.getenv("GOROOT") ?: ""
        }
    }
}