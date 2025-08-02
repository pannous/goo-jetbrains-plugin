package com.pannous.goo.formatting

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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
        val languageCheck = file.language == GooLanguage
        val formatterAvailable = isFormatterAvailable(file.project)
        val canFormat = languageCheck && formatterAvailable
        
        println("GooExternalFormatter: canFormat called for ${file.name}")
        println("GooExternalFormatter: Language is Goo: $languageCheck (${file.language})")
        println("GooExternalFormatter: Formatter available: $formatterAvailable")
        println("GooExternalFormatter: Final result: $canFormat")
        
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
        
        // Get gofmt path - force bundled for now to test
        val gofmtPath = "/opt/other/go/bin/gofmt"
        
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
        
        println("GooExternalFormatter: Checking formatter availability...")
        
        if (!settings.isCompilerIntegrationEnabled()) {
            println("GooExternalFormatter: Compiler integration disabled")
            return false
        }
        
        return try {
            val compilerPath = settings.getCompilerPath()
            val goRoot = settings.getGoRoot()
            
            println("GooExternalFormatter: Compiler path: $compilerPath")
            println("GooExternalFormatter: GOROOT: $goRoot")
            
            // Check if gofmt is available - force bundled path for now
            val gofmtPath = "/opt/other/go/bin/gofmt"
            
            println("GooExternalFormatter: Using bundled compiler: ${settings.isUsingBundledCompiler()}")
            println("GooExternalFormatter: File exists at bundled path: ${java.io.File(gofmtPath).exists()}")
            
            println("GooExternalFormatter: gofmt path: $gofmtPath")
            
            val processBuilder = ProcessBuilder().apply {
                command(listOf(gofmtPath, "-h"))
                environment()["GOROOT"] = goRoot
            }
            
            val process = processBuilder.start()
            val exitCode = if (process.waitFor(5, TimeUnit.SECONDS)) process.exitValue() else -1
            val success = exitCode == 0 // gofmt -h exits with 0
            
            println("GooExternalFormatter: gofmt exit code: $exitCode")
            println("GooExternalFormatter: gofmt test result: $success")
            success
        } catch (e: Exception) {
            println("GooExternalFormatter: Exception checking formatter: ${e.message}")
            false
        }
    }
}