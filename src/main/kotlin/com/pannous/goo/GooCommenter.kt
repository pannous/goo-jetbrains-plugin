package com.pannous.goo

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.pannous.goo.lexer.GooTokenTypes

class GooCommenter : CodeDocumentationAwareCommenter {
    
    // Primary comment prefix (what we'll add when commenting)
    override fun getLineCommentPrefix(): String = "#"
    
    override fun getBlockCommentPrefix(): String = "/*"
    
    override fun getBlockCommentSuffix(): String = "*/"
    
    override fun getCommentedBlockCommentPrefix(): String? = null
    
    override fun getCommentedBlockCommentSuffix(): String? = null
    
    // Return line comment token type for proper handling
    override fun getLineCommentTokenType(): IElementType = GooTokenTypes.COMMENT
    
    override fun getBlockCommentTokenType(): IElementType? = null
    
    override fun getDocumentationCommentTokenType(): IElementType? = null
    
    override fun getDocumentationCommentPrefix(): String? = null
    
    override fun getDocumentationCommentLinePrefix(): String? = null
    
    override fun getDocumentationCommentSuffix(): String? = null
    
    override fun isDocumentationComment(element: PsiComment): Boolean = false
    
    // Custom method to check if a line is commented with either # or //
    fun isLineCommented(lineText: String): Boolean {
        val trimmed = lineText.trim()
        return trimmed.startsWith("#") || trimmed.startsWith("//")
    }
    
    // Custom method to uncomment a line, handling both # and // styles
    fun uncommentLine(lineText: String): String {
        val trimmed = lineText.trimStart()
        return when {
            trimmed.startsWith("# ") -> lineText.replaceFirst("# ", "")
            trimmed.startsWith("#") -> lineText.replaceFirst("#", "")
            trimmed.startsWith("// ") -> lineText.replaceFirst("// ", "")
            trimmed.startsWith("//") -> lineText.replaceFirst("//", "")
            else -> lineText
        }
    }
    
    // Custom method to comment a line using the preferred style
    fun commentLine(lineText: String): String {
        return if (lineText.trimStart().isEmpty()) {
            lineText  // Don't comment empty lines
        } else {
            val leadingWhitespace = lineText.takeWhile { it.isWhitespace() }
            val content = lineText.trimStart()
            "$leadingWhitespace# $content"
        }
    }
}