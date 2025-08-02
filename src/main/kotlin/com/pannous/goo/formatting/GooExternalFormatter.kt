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
        return file.language == GooLanguage && isFormatterAvailable(file.project)
    }
    
    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val file = request.context.containingFile
        val project = file.project
        
        if (!canFormat(file)) return null
        
        return object : FormattingTask {
            override fun run() {
                try {
                    val formattedText = formatGooCode(
                        request.documentText,
                        project,
                        request.formattingRanges
                    )
                    
                    if (formattedText != null && formattedText != request.documentText) {
                        request.onTextReady(formattedText)
                    }
                } catch (e: Exception) {
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
        
        return try {
            // Create temporary file with .goo extension
            val tempFile = File.createTempFile("goo_format_", ".goo").apply {
                writeText(text)
                deleteOnExit()
            }
            
            // Run gofmt through Goo compiler
            val processBuilder = ProcessBuilder().apply {
                command(listOf(compilerPath, "fmt", tempFile.absolutePath))
                environment()["GOROOT"] = goRoot
                environment()["GOO_USE_TRANSFORMERS"] = "1"
                redirectErrorStream(true) // Capture errors for debugging
            }
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            
            val success = process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0
            
            if (success) {
                // gofmt modifies the file in place, so read the formatted content
                val formattedContent = tempFile.readText()
                if (formattedContent.isNotEmpty() && formattedContent != text) {
                    formattedContent
                } else {
                    null // No changes made
                }
            } else {
                // If formatter fails, log error but don't block
                if (output.isNotEmpty()) {
                    println("Goo formatter warning: $output")
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
            
            val processBuilder = ProcessBuilder().apply {
                command(listOf(compilerPath, "help", "fmt"))
                environment()["GOROOT"] = goRoot
            }
            
            val process = processBuilder.start()
            val success = process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0
            success
        } catch (e: Exception) {
            false
        }
    }
}