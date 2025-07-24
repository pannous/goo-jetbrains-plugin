package com.pannous.goo.formatting

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.pannous.goo.psi.GooFile
import java.io.File
import java.util.*

class GooExternalFormatter : AsyncDocumentFormattingService() {
    
    override fun canFormat(file: PsiFile): Boolean {
        return file is GooFile
    }
    
    override fun getFeatures(): Set<FormattingService.Feature> {
        return EnumSet.of(FormattingService.Feature.FORMAT_FRAGMENTS)
    }
    
    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val file = request.context.containingFile
        if (file !is GooFile) return null
        
        return object : FormattingTask {
            override fun run() {
                val formattedText = formatWithGofmt(request.documentText, file.project)
                if (formattedText != null && formattedText != request.documentText) {
                    request.onTextReady(formattedText)
                }
            }
            
            override fun cancel(): Boolean = false
            override fun isRunUnderProgress(): Boolean = true
        }
    }
    
    override fun getNotificationGroupId(): String = "Goo Formatter"
    override fun getName(): String = "Goo External Formatter"
    
    private fun formatWithGofmt(content: String, project: Project): String? {
        try {
            val gofmtPath = findGofmtBinary()
            if (gofmtPath == null) {
                // Silently fallback to original content if gofmt not found
                return content
            }
            
            // Skip formatting if content is empty or just whitespace
            if (content.isBlank()) {
                return content
            }
            
            val process = ProcessBuilder(gofmtPath)
                .redirectErrorStream(false)
                .start()
            
            // Write content to gofmt stdin and close it
            process.outputStream.use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
            }
            
            // Read output and error streams
            val formattedContent = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val errorOutput = process.errorStream.bufferedReader(Charsets.UTF_8).readText()
            
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                return if (formattedContent.isNotEmpty()) formattedContent else content
            } else {
                // If gofmt fails due to syntax errors, return original content
                // This prevents formatter from breaking during editing
                if (errorOutput.isNotEmpty()) {
                    println("gofmt formatting failed (exit code: $exitCode): $errorOutput")
                }
                return content
            }
        } catch (e: Exception) {
            // Silently handle errors and return original content
            // This ensures formatting never breaks the editing experience
            return content
        }
    }
    
    private fun findGofmtBinary(): String? {
        // 1. Try GOROOT/bin/gofmt
        val goroot = System.getenv("GOROOT")
        if (!goroot.isNullOrEmpty()) {
            val gorootGofmt = File(goroot, "bin/gofmt")
            if (gorootGofmt.exists() && gorootGofmt.canExecute()) {
                return gorootGofmt.absolutePath
            }
        }
        
        // 2. Try to infer from go binary location
        val goBinary = findGoBinary()
        if (goBinary != null) {
            val gofmt = File(File(goBinary).parent, "gofmt")
            if (gofmt.exists() && gofmt.canExecute()) {
                return gofmt.absolutePath
            }
        }
        
        // 3. Try system PATH
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val gofmt = File(pathDir, "gofmt")
            if (gofmt.exists() && gofmt.canExecute()) {
                return gofmt.absolutePath
            }
        }
        
        return null
    }
    
    private fun findGoBinary(): String? {
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (pathDir in pathDirs) {
            val go = File(pathDir, "go")
            if (go.exists() && go.canExecute()) {
                return go.absolutePath
            }
        }
        return null
    }
}