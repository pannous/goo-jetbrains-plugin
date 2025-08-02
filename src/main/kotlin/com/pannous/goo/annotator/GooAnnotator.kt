package com.pannous.goo.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.pannous.goo.compiler.GooCompiler
import com.pannous.goo.psi.GooFile
import com.pannous.goo.services.GooCompilerService

/**
 * Annotator that uses the Goo compiler to provide real-time error highlighting
 * and semantic analysis in the IDE.
 */
class GooAnnotator : Annotator {
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Only process the root file element to avoid multiple compiler calls
        if (element !is GooFile) return
        
        val virtualFile = element.virtualFile ?: return
        val project = element.project
        
        try {
            val compilerService = GooCompilerService.getInstance(project)
            
            // Skip if compiler is not available
            if (!compilerService.isCompilerAvailable()) {
                return
            }
            
            // Get compiler diagnostics
            val result = compilerService.getDiagnostics(virtualFile)
            
            // Process each error from the compiler
            for (error in result.errors) {
                // Only show errors for the current file
                if (!error.file.endsWith(virtualFile.name)) continue
                
                // Calculate text range for the error
                val textRange = getTextRangeForError(element, error)
                if (textRange == null) continue
                
                // Create annotation based on error severity
                when (error.severity) {
                    GooCompiler.ErrorSeverity.ERROR -> {
                        holder.newAnnotation(HighlightSeverity.ERROR, error.message)
                            .range(textRange)
                            .textAttributes(DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
                            .create()
                    }
                    GooCompiler.ErrorSeverity.WARNING -> {
                        holder.newAnnotation(HighlightSeverity.WARNING, error.message)
                            .range(textRange)
                            .textAttributes(DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE)
                            .create()
                    }
                    GooCompiler.ErrorSeverity.INFO -> {
                        holder.newAnnotation(HighlightSeverity.INFORMATION, error.message)
                            .range(textRange)
                            .textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
                            .create()
                    }
                }
            }
        } catch (e: Exception) {
            // Silently ignore compiler errors to avoid disrupting the IDE
            // In production, you might want to log this for debugging
        }
    }
    
    /**
     * Calculate the text range for a compiler error based on line and column numbers
     */
    private fun getTextRangeForError(file: GooFile, error: GooCompiler.CompilerError): TextRange? {
        val document = file.viewProvider.document ?: return null
        
        // Convert to 0-based indexing (compiler uses 1-based)
        val line = (error.line - 1).coerceAtLeast(0)
        val column = (error.column - 1).coerceAtLeast(0)
        
        // Check if line number is valid
        if (line >= document.lineCount) return null
        
        val lineStartOffset = document.getLineStartOffset(line)
        val lineEndOffset = document.getLineEndOffset(line)
        val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
        
        // Calculate the actual offset, ensuring it's within the line
        val errorOffset = lineStartOffset + column.coerceAtMost(lineText.length)
        
        // Try to highlight the problematic token/word
        val endOffset = findTokenEnd(lineText, column, lineEndOffset, errorOffset)
        
        return TextRange(errorOffset, endOffset)
    }
    
    /**
     * Find the end of the token that contains the error
     */
    private fun findTokenEnd(lineText: String, column: Int, lineEndOffset: Int, errorOffset: Int): Int {
        var endOffset = errorOffset
        
        // If we're at a valid column position in the line
        if (column < lineText.length) {
            // Find the end of the current token (word boundary)
            while (endOffset < lineEndOffset && column + (endOffset - errorOffset) < lineText.length) {
                val char = lineText[column + (endOffset - errorOffset)]
                if (!char.isLetterOrDigit() && char != '_') break
                endOffset++
            }
        }
        
        // Ensure we highlight at least one character
        if (endOffset == errorOffset) {
            endOffset = (errorOffset + 1).coerceAtMost(lineEndOffset)
        }
        
        return endOffset
    }
}