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
        val parentText = myNode.treeParent?.text
        
        // Simple indentation logic for Go-style formatting
        return when {
            // Opening brace - no additional indent
            nodeText == "{" -> Indent.getNoneIndent()
            
            // Closing brace - no additional indent  
            nodeText == "}" -> Indent.getNoneIndent()
            
            // If parent contains braces and this isn't a brace, indent it
            parentText != null && parentText.contains("{") && parentText.contains("}") && 
            nodeText != "{" && nodeText != "}" -> Indent.getNormalIndent()
            
            // Default: no indent
            else -> Indent.getNoneIndent()
        }
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
            // Inside braces, indent new children
            nodeText.contains("{") && !nodeText.trim().endsWith("}") -> 
                ChildAttributes(Indent.getNormalIndent(), null)
            
            else -> ChildAttributes(Indent.getNoneIndent(), null)
        }
    }
}