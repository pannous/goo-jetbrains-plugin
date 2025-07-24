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
        val parentType = myNode.treeParent?.elementType
        val nodeType = myNode.elementType
        val nodeText = myNode.text
        
        return when {
            // Indent inside braces - Go style
            parentType?.toString()?.contains("BLOCK") == true -> Indent.getNormalIndent()
            
            // Check for braces in text content
            nodeText == "{" || nodeText == "}" -> {
                // Braces align with their parent
                Indent.getNoneIndent()
            }
            
            // Content inside braces should be indented
            myNode.treeParent?.text?.contains("{") == true -> {
                if (nodeText.trim() != "}" && nodeText.trim() != "{") {
                    Indent.getNormalIndent()
                } else {
                    Indent.getNoneIndent()
                }
            }
            
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