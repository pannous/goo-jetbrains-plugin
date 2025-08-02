package com.pannous.goo.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.pannous.goo.settings.GooSettings
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Interface to the Goo compiler for IDE integration.
 * Provides syntax checking, AST parsing, and semantic analysis.
 */
class GooCompiler(private val project: Project) {
    
    private val settings = GooSettings.getInstance(project)
    private val compilerPath get() = settings.getCompilerPath()
    private val goRoot get() = settings.getGoRoot()
    
    data class CompilerResult(
        val success: Boolean,
        val output: String,
        val errors: List<CompilerError>
    )
    
    data class CompilerError(
        val file: String,
        val line: Int,
        val column: Int,
        val message: String,
        val severity: ErrorSeverity = ErrorSeverity.ERROR
    )
    
    enum class ErrorSeverity {
        ERROR, WARNING, INFO
    }
    
    /**
     * Check syntax of a Goo file without compilation
     */
    fun checkSyntax(file: VirtualFile): CompilerResult {
        return runCompiler(listOf("run", "-n", file.path))
    }
    
    /**
     * Parse file and return syntax errors
     */
    fun parseFile(file: VirtualFile): CompilerResult {
        // Use build with -n flag to parse without actual compilation
        return runCompiler(listOf("build", "-n", file.path))
    }
    
    /**
     * Get compiler diagnostics with detailed error information
     */
    fun getDiagnostics(file: VirtualFile): CompilerResult {
        // Try to build the file and capture all output
        return runCompiler(listOf("build", "-v", file.path))
    }
    
    /**
     * Run the Goo compiler with given arguments
     */
    private fun runCompiler(args: List<String>): CompilerResult {
        return try {
            val processBuilder = ProcessBuilder().apply {
                command(listOf(compilerPath) + args)
                environment()["GOROOT"] = goRoot
                environment()["GOO_USE_TRANSFORMERS"] = "1"
                redirectErrorStream(true)
            }
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val success = process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0
            
            val errors = parseCompilerOutput(output)
            
            CompilerResult(success, output, errors)
        } catch (e: IOException) {
            CompilerResult(false, "Failed to run compiler: ${e.message}", emptyList())
        } catch (e: InterruptedException) {
            CompilerResult(false, "Compiler timeout", emptyList())
        }
    }
    
    /**
     * Parse compiler error output into structured error objects
     */
    private fun parseCompilerOutput(output: String): List<CompilerError> {
        val errors = mutableListOf<CompilerError>()
        val lines = output.split('\n')
        
        for (line in lines) {
            // Parse Go compiler error format: file:line:col: message
            val errorPattern = Regex("""^(.+):(\d+):(\d+):\s*(.+)$""")
            val match = errorPattern.matchEntire(line.trim())
            
            if (match != null) {
                val (file, lineStr, colStr, message) = match.destructured
                val lineNum = lineStr.toIntOrNull() ?: 0
                val colNum = colStr.toIntOrNull() ?: 0
                
                val severity = when {
                    message.contains("error", ignoreCase = true) -> ErrorSeverity.ERROR
                    message.contains("warning", ignoreCase = true) -> ErrorSeverity.WARNING
                    else -> ErrorSeverity.ERROR
                }
                
                errors.add(CompilerError(file, lineNum, colNum, message, severity))
            }
        }
        
        return errors
    }
    
    /**
     * Check if the Goo compiler is available and working
     */
    fun isCompilerAvailable(): Boolean {
        // Check if compiler integration is enabled
        if (!settings.isCompilerIntegrationEnabled()) {
            return false
        }
        
        return try {
            val result = runCompiler(listOf("version"))
            result.success
        } catch (e: Exception) {
            false
        }
    }
}