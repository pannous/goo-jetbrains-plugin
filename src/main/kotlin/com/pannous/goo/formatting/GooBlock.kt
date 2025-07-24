package com.pannous.goo.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.AbstractBlock
import com.pannous.goo.lexer.GooTokenTypes

class GooBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode
        
        while (child != null) {
            if (child.textRange.length > 0) {
                blocks.add(
                    GooBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        spacingBuilder
                    )
                )
            }
            child = child.treeNext
        }
        
        return blocks
    }

    override fun getIndent(): Indent? {
        val nodeText = myNode.text.trim()
        
        // DEBUG: Always indent non-brace, non-keyword content
        return when {
            // Top-level keywords should never be indented
            nodeText.startsWith("package") || 
            nodeText.startsWith("import") ||
            nodeText.startsWith("func") ||
            nodeText.startsWith("def") ||
            nodeText.startsWith("void") ||
            nodeText.startsWith("var") ||
            nodeText.startsWith("const") ||
            nodeText.startsWith("type") -> Indent.getNoneIndent()
            
            // Opening brace - no additional indent
            nodeText == "{" -> Indent.getNoneIndent()
            
            // Closing brace - no additional indent  
            nodeText == "}" -> Indent.getNoneIndent()
            
            // For debugging: if we're not at the start of the file and not a brace/keyword,
            // and we have a parent, try indenting
            myNode.treeParent != null && 
            nodeText.isNotEmpty() && 
            !nodeText.matches(Regex("^\\s*$")) -> Indent.getNormalIndent()
            
            // Default: no indent for everything else
            else -> Indent.getNoneIndent()
        }
    }
    
    private fun isInsideBlock(): Boolean {
        // Simple approach: look at the node's text content and its ancestors
        var current = myNode.treeParent
        var braceDepth = 0
        
        while (current != null) {
            val currentText = current.text
            
            // Count opening and closing braces in ancestor nodes
            val openBraces = currentText.count { it == '{' }
            val closeBraces = currentText.count { it == '}' }
            
            // If we find an ancestor with more opening braces than closing braces,
            // we're likely inside a block
            if (openBraces > closeBraces) {
                braceDepth += (openBraces - closeBraces)
            }
            
            current = current.treeParent
        }
        
        // If we have positive brace depth, we're inside a block
        return braceDepth > 0
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val nodeText = myNode.text
        
        return when {
            // If this node contains an opening brace, indent its children
            nodeText.contains("{") -> ChildAttributes(Indent.getNormalIndent(), null)
            
            else -> ChildAttributes(Indent.getNoneIndent(), null)
        }
    }
}