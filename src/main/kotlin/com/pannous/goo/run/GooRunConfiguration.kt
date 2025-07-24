package com.pannous.goo.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.Logger
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

    companion object {
        private val LOG = Logger.getInstance(GooRunConfiguration::class.java)
        
        init {
            LOG.warn("=== GooRunConfiguration CLASS LOADED ===")
        }
    }

    var scriptName = ""
    var filePath = ""
    var workingDirectory = ""
    
    init {
        LOG.warn("=== GooRunConfiguration INSTANCE CREATED: project=${project.name}, name=$name ===")
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        LOG.info("GooRunConfiguration: Creating configuration editor")
        return object : SettingsEditor<GooRunConfiguration>() {
            override fun resetEditorFrom(s: GooRunConfiguration) {
                LOG.info("GooRunConfiguration: resetEditorFrom called with filePath='${s.filePath}', workingDirectory='${s.workingDirectory}'")
            }
            override fun applyEditorTo(s: GooRunConfiguration) {
                LOG.info("GooRunConfiguration: applyEditorTo called") 
            }
            override fun createEditor(): JComponent {
                LOG.info("GooRunConfiguration: createEditor called")
                return JLabel("Goo Run Configuration")
            }
        }
    }

    override fun checkConfiguration() {
        LOG.info("GooRunConfiguration: checkConfiguration called - filePath='$filePath', scriptName='$scriptName'")
        
        if (filePath.isEmpty() && scriptName.isEmpty()) {
            LOG.warn("GooRunConfiguration: Both filePath and scriptName are empty!")
            throw RuntimeConfigurationError("File path is not specified. Please set either filePath or scriptName.")
        }
        
        val actualPath = filePath.ifEmpty { scriptName }
        LOG.info("GooRunConfiguration: Using actualPath='$actualPath'")
        
        if (actualPath.isNotEmpty()) {
            val file = File(actualPath)
            if (!file.exists()) {
                LOG.warn("GooRunConfiguration: File does not exist: $actualPath")
                throw RuntimeConfigurationError("File does not exist: $actualPath")
            }
            if (!file.canRead()) {
                LOG.warn("GooRunConfiguration: File is not readable: $actualPath")
                throw RuntimeConfigurationError("File is not readable: $actualPath")
            }
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        LOG.info("GooRunConfiguration: getState called")
        LOG.info("GooRunConfiguration: Current state - filePath='$filePath', scriptName='$scriptName', workingDirectory='$workingDirectory'")
        
        return object : RunProfileState {
            override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): com.intellij.execution.ExecutionResult {
                LOG.info("GooRunConfiguration: execute called")
                
                try {
                    val commandLine = GeneralCommandLine()
                    
                    // Find working Go binary
                    val goPath = findWorkingGo()
                    LOG.info("GooRunConfiguration: Using Go binary: $goPath")
                    
                    commandLine.exePath = goPath
                    
                    // Determine which file to run
                    val targetFile = filePath.ifEmpty { scriptName }
                    LOG.info("GooRunConfiguration: Target file: '$targetFile'")
                    
                    if (targetFile.isEmpty()) {
                        throw RuntimeConfigurationError("No file specified to run. Both filePath and scriptName are empty.")
                    }
                    
                    if (targetFile.endsWith(".goo")) {
                        // For .goo files, use 'go run' (this might fail with standard Go)
                        commandLine.addParameter("run")
                        commandLine.addParameter(targetFile)
                        LOG.info("GooRunConfiguration: Running .goo file with 'go run'")
                    } else {
                        // For other files, just execute
                        commandLine.addParameter("version") // Safe test command
                        LOG.info("GooRunConfiguration: Running 'go version' for testing")
                    }
                    
                    // Set working directory
                    val workDir = determineWorkingDirectory(targetFile)
                    LOG.info("GooRunConfiguration: Working directory: '$workDir'")
                    
                    if (workDir.isNotEmpty()) {
                        val workDirFile = File(workDir)
                        if (workDirFile.exists() && workDirFile.isDirectory()) {
                            commandLine.workDirectory = workDirFile
                        } else {
                            LOG.warn("GooRunConfiguration: Working directory does not exist or is not a directory: $workDir")
                            throw RuntimeConfigurationError("Working directory does not exist: $workDir")
                        }
                    }
                    
                    // Set environment
                    commandLine.environment.putAll(System.getenv())
                    val goroot = getGOROOTForPath(goPath)
                    if (goroot.isNotEmpty()) {
                        commandLine.environment["GOROOT"] = goroot
                        LOG.info("GooRunConfiguration: Set GOROOT to: $goroot")
                    }
                    
                    LOG.info("GooRunConfiguration: Final command: ${commandLine.commandLineString}")
                    LOG.info("GooRunConfiguration: Working directory: ${commandLine.workDirectory?.absolutePath}")
                    LOG.info("GooRunConfiguration: Environment GOROOT: ${commandLine.environment["GOROOT"]}")
                    
                    val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                    LOG.info("GooRunConfiguration: Process handler created successfully")
                    
                    return com.intellij.execution.DefaultExecutionResult(null, processHandler)
                    
                } catch (e: Exception) {
                    LOG.error("GooRunConfiguration: Error in execute()", e)
                    throw RuntimeConfigurationError("Failed to execute Goo file: ${e.message}")
                }
            }
        }
    }
    
    private fun determineWorkingDirectory(targetFile: String): String {
        LOG.info("GooRunConfiguration: determineWorkingDirectory called with targetFile='$targetFile'")
        
        // Priority: explicit workingDirectory > parent of target file > project base path
        if (workingDirectory.isNotEmpty()) {
            LOG.info("GooRunConfiguration: Using explicit working directory: $workingDirectory")
            return workingDirectory
        }
        
        if (targetFile.isNotEmpty()) {
            val file = File(targetFile)
            val parent = if (file.isAbsolute) {
                file.parent
            } else {
                File(project.basePath ?: ".", targetFile).parent
            }
            if (parent != null) {
                LOG.info("GooRunConfiguration: Using parent directory of target file: $parent")
                return parent
            }
        }
        
        val projectBase = project.basePath ?: "."
        LOG.info("GooRunConfiguration: Using project base path: $projectBase")
        return projectBase
    }
    
    private fun findWorkingGo(): String {
        LOG.info("GooRunConfiguration: findWorkingGo called")
        
        val candidates = listOf(
            "/opt/homebrew/bin/go",  // Homebrew Go (usually works)
            "/usr/local/bin/go",     // Manual install  
            "/opt/other/go/bin/go"   // Your Goo (but might be broken)
        )
        
        for (path in candidates) {
            LOG.info("GooRunConfiguration: Testing Go candidate: $path")
            if (File(path).exists() && isWorkingGo(path)) {
                LOG.info("GooRunConfiguration: Found working Go: $path")
                return path
            } else {
                LOG.info("GooRunConfiguration: Candidate $path failed (exists=${File(path).exists()}, works=${isWorkingGo(path)})")
            }
        }
        
        LOG.info("GooRunConfiguration: No candidates worked, falling back to PATH 'go'")
        return "go"
    }
    
    private fun isWorkingGo(path: String): Boolean {
        return try {
            LOG.info("GooRunConfiguration: Testing if $path works")
            val process = ProcessBuilder(path, "version").start()
            val exitCode = process.waitFor()
            val success = exitCode == 0
            LOG.info("GooRunConfiguration: $path test result: exitCode=$exitCode, success=$success")
            success
        } catch (e: Exception) {
            LOG.warn("GooRunConfiguration: Exception testing $path", e)
            false
        }
    }
    
    private fun getGOROOTForPath(goPath: String): String {
        val goroot = when {
            goPath.contains("/opt/homebrew/") -> "/opt/homebrew/Cellar/go/1.24.5/libexec"
            goPath.contains("/usr/local/") -> "/usr/local/go"
            goPath.contains("/opt/other/go/") -> "/opt/other/go"
            else -> System.getenv("GOROOT") ?: ""
        }
        LOG.info("GooRunConfiguration: Determined GOROOT '$goroot' for Go path '$goPath'")
        return goroot
    }
}