package com.pannous.goo.formatting

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.pannous.goo.GooLanguage
import com.pannous.goo.settings.GooSettings
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * External formatter that uses the Goo compiler's gofmt tool for proper formatting.
 * Provides Go-style formatting with Goo language extensions.
 */
class GooExternalFormatter : AsyncDocumentFormattingService() {
    
    override fun getNotificationGroupId(): String = "Goo External Formatter"
    
    override fun getName(): String = "Goo Formatter"
    
    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return mutableSetOf(FormattingService.Feature.FORMAT_FRAGMENTS)
    }
    
    override fun canFormat(file: PsiFile): Boolean {
        val canFormat = file.language == GooLanguage && isFormatterAvailable(file.project)
        println("GooExternalFormatter: canFormat called for ${file.name}, result: $canFormat")
        return canFormat
    }
    
    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val file = request.context.containingFile
        val project = file.project
        
        if (!canFormat(file)) return null
        
        return object : FormattingTask {
            override fun run() {
                try {
                    println("GooExternalFormatter: Starting format task")
                    println("GooExternalFormatter: Input text length: ${request.documentText.length}")
                    
                    val formattedText = formatGooCode(
                        request.documentText,
                        project,
                        request.formattingRanges
                    )
                    
                    println("GooExternalFormatter: Formatted text length: ${formattedText?.length ?: "null"}")
                    println("GooExternalFormatter: Text changed: ${formattedText != null && formattedText != request.documentText}")
                    
                    if (formattedText != null && formattedText != request.documentText) {
                        request.onTextReady(formattedText)
                    } else {
                        println("GooExternalFormatter: No changes made or formatting failed")
                    }
                } catch (e: Exception) {
                    println("GooExternalFormatter: Error - ${e.message}")
                    request.onError("Goo formatting failed", e.message ?: "Unknown error")
                }
            }
            
            override fun cancel(): Boolean = true
            
            override fun isRunUnderProgress(): Boolean = true
        }
    }
    
    /**
     * Format Goo code using the external gofmt tool
     */
    private fun formatGooCode(
        text: String, 
        project: Project, 
        ranges: List<TextRange>
    ): String? {
        val settings = GooSettings.getInstance(project)
        
        if (!settings.isCompilerIntegrationEnabled()) {
            return null
        }
        
        val compilerPath = settings.getCompilerPath()
        val goRoot = settings.getGoRoot()
        
        // Get gofmt path based on bundled compiler setting
        val gofmtPath = if (settings.isUsingBundledCompiler()) {
            // Use gofmt from the same directory as the bundled compiler
            if (compilerPath.endsWith("/bin/go")) {
                compilerPath.replace("/bin/go", "/bin/gofmt")
            } else {
                // Try bundled locations directly
                val bundledPaths = listOf(
                    "/opt/other/go/bin/gofmt",
                    "/usr/local/go/bin/gofmt",
                    "/usr/local/goo/bin/gofmt"
                )
                bundledPaths.find { java.io.File(it).exists() } ?: "gofmt"
            }
        } else {
            // Use system gofmt from PATH
            "gofmt"
        }
        
        return try {
            // Create temporary file with .goo extension so gofmt knows it's Goo syntax
            val tempFile = File.createTempFile("goo_format_", ".goo").apply {
                writeText(text)
                deleteOnExit()
            }
            
            // Run gofmt on the temporary file
            val processBuilder = ProcessBuilder().apply {
                command(listOf(gofmtPath, tempFile.absolutePath))
                environment()["GOROOT"] = goRoot
                redirectErrorStream(false) // Keep stdout and stderr separate
            }
            
            val process = processBuilder.start()
            
            // Read formatted output from stdout
            val formattedOutput = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()
            
            val success = process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0
            
            println("GooExternalFormatter: Process success: $success, exit code: ${if (process.isAlive) "still running" else process.exitValue()}")
            println("GooExternalFormatter: Formatted output length: ${formattedOutput.length}")
            println("GooExternalFormatter: Error output: '$errorOutput'")
            
            if (success) {
                if (formattedOutput.isNotEmpty()) {
                    // Normalize line endings for comparison
                    val normalizedInput = text.replace("\r\n", "\n").replace("\r", "\n")
                    val normalizedOutput = formattedOutput.replace("\r\n", "\n").replace("\r", "\n")
                    
                    println("GooExternalFormatter: Input length: ${normalizedInput.length}")
                    println("GooExternalFormatter: Output length: ${normalizedOutput.length}")
                    println("GooExternalFormatter: Are equal: ${normalizedInput == normalizedOutput}")
                    
                    // Debug: show first 200 chars of each
                    println("GooExternalFormatter: Input preview: '${normalizedInput.take(200)}'")
                    println("GooExternalFormatter: Output preview: '${normalizedOutput.take(200)}'")
                    
                    if (normalizedOutput != normalizedInput) {
                        println("GooExternalFormatter: Returning formatted text")
                        formattedOutput
                    } else {
                        println("GooExternalFormatter: No changes detected - FORCING a change for testing")
                        // Force a change by adding a comment to test if formatter is working
                        "$formattedOutput\n// Formatted by Goo"
                    }
                } else {
                    println("GooExternalFormatter: Empty output")
                    null
                }
            } else {
                // If formatter fails, log error but don't block
                if (errorOutput.isNotEmpty()) {
                    println("Goo formatter error: $errorOutput")
                }
                null
            }
        } catch (e: IOException) {
            println("Goo formatter error: ${e.message}")
            null
        } catch (e: InterruptedException) {
            println("Goo formatter timeout")
            null
        }
    }
    
    /**
     * Check if the Goo formatter is available
     */
    private fun isFormatterAvailable(project: Project): Boolean {
        val settings = GooSettings.getInstance(project)
        
        if (!settings.isCompilerIntegrationEnabled()) {
            return false
        }
        
        return try {
            val compilerPath = settings.getCompilerPath()
            val goRoot = settings.getGoRoot()
            
            // Check if gofmt is available - use same logic as formatter
            val gofmtPath = if (settings.isUsingBundledCompiler()) {
                if (compilerPath.endsWith("/bin/go")) {
                    compilerPath.replace("/bin/go", "/bin/gofmt")
                } else {
                    val bundledPaths = listOf(
                        "/opt/other/go/bin/gofmt",
                        "/usr/local/go/bin/gofmt",
                        "/usr/local/goo/bin/gofmt"
                    )
                    bundledPaths.find { java.io.File(it).exists() } ?: "gofmt"
                }
            } else {
                "gofmt"
            }
            
            val processBuilder = ProcessBuilder().apply {
                command(listOf(gofmtPath, "-h"))
                environment()["GOROOT"] = goRoot
            }
            
            val process = processBuilder.start()
            val success = process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 2 // gofmt -h exits with 2
            success
        } catch (e: Exception) {
            false
        }
    }
}