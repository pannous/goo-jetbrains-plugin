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
        
        // Simple indentation logic for Go-style formatting
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
            
            // Check if we're inside a block by walking up the AST
            isInsideBlock() -> Indent.getNormalIndent()
            
            // Default: no indent for everything else
            else -> Indent.getNoneIndent()
        }
    }
    
    private fun isInsideBlock(): Boolean {
        var current = myNode.treeParent
        
        while (current != null) {
            val currentText = current.text
            
            // Look for a parent that contains braces indicating a block structure
            if (currentText.contains("{") && currentText.contains("}")) {
                // Make sure we're actually between the braces, not outside them
                val nodeStart = myNode.startOffset
                val openBraceIndex = currentText.indexOf('{')
                val closeBraceIndex = currentText.lastIndexOf('}')
                
                if (openBraceIndex >= 0 && closeBraceIndex >= 0) {
                    val parentStart = current.startOffset
                    val openBracePos = parentStart + openBraceIndex
                    val closeBracePos = parentStart + closeBraceIndex
                    
                    // We're inside the block if our position is between the braces
                    if (nodeStart > openBracePos && nodeStart < closeBracePos) {
                        return true
                    }
                }
            }
            
            current = current.treeParent
        }
        
        return false
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