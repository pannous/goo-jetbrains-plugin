package com.pannous.goo.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.AbstractBlock
import com.pannous.goo.lexer.GooTokenTypes

class GooBlock(
    private val node: ASTNode,
    private val wrap: Wrap?,
    private val alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : ASTBlock {

    override fun getNode() = node
    override fun getTextRange(): TextRange = node.textRange
    override fun getWrap(): Wrap? = wrap
    override fun getAlignment(): Alignment? = alignment
    override fun getSubBlocks(): List<Block> = buildChildren()

    override fun isLeaf(): Boolean = node.firstChildNode == null

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getIndent(): Indent? {
        val type = node.elementType
        val parentType = node.treeParent?.elementType

        return when {
            // Indent inside { ... }
            parentType == GooTokenTypes.LBRACE -> Indent.getNormalIndent()
            parentType == GooTokenTypes.RBRACE -> Indent.getNoneIndent()
            else -> Indent.getNoneIndent()
        }
    }

    private fun buildChildren(): List<Block> {
        val result = mutableListOf<Block>()
        var child = node.firstChildNode
        while (child != null) {
            if (child.textRange.length > 0) {
                result.add(
                    GooBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        spacingBuilder
                    )
                )
            }
            child = child.treeNext
        }
        return result
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes =
        ChildAttributes(Indent.getNormalIndent(), null)

    override fun isIncomplete(): Boolean = false
}

// class GooBlock(
//     node: ASTNode,
//     wrap: Wrap?,
//     alignment: Alignment?,
//     private val spacingBuilder: SpacingBuilder
// ) : AbstractBlock(node, wrap, alignment) {

//  override fun buildChildren(): List<Block> =
//     node.getChildren(null)
//       .filter { it.textRange.length > 0 }
//       .map {
//         GooBlock(it, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment(), spacingBuilder)
//       }

//   override fun getIndent(): Indent? = when (node.elementType) {
//     GooTokenTypes.LBRACE, GooTokenTypes.RBRACE -> Indent.getNoneIndent()
//     else -> if (node.treeParent?.elementType == GooElementTypes.BLOCK) Indent.getNormalIndent() else Indent.getNoneIndent()
//   }

//   override fun getSpacing(child1: Block?, child2: Block): Spacing? =
//     spacingBuilder.getSpacing(this, child2)

// /*
//     override fun buildChildren(): List<Block> {
//         val blocks = mutableListOf<Block>()
//         var child = myNode.firstChildNode
        
//         while (child != null) {
//             if (child.textRange.length > 0) {
//                 blocks.add(
//                     GooBlock(
//                         child,
//                         Wrap.createWrap(WrapType.NONE, false),
//                         null,
//                         spacingBuilder
//                     )
//                 )
//             }
//             child = child.treeNext
//         }
        
//         return blocks
//     }
//     override fun getIndent(): Indent? {
//         val nodeText = myNode.text.trim()
        
//         return when {
//             // Top-level keywords should never be indented
//             nodeText.startsWith("package") || 
//             nodeText.startsWith("import") ||
//             nodeText.startsWith("func") ||
//             nodeText.startsWith("def") ||
//             nodeText.startsWith("void") ||
//             nodeText.startsWith("var") ||
//             nodeText.startsWith("const") ||
//             nodeText.startsWith("type") -> Indent.getNoneIndent()
            
//             // Opening brace - no additional indent
//             nodeText == "{" -> Indent.getNoneIndent()
            
//             // Closing brace - no additional indent  
//             nodeText == "}" -> Indent.getNoneIndent()
            
//             // More aggressive: if we have ANY ancestor that contains braces, indent
//             hasBlockAncestor() -> Indent.getNormalIndent()
            
//             // Default: no indent
//             else -> Indent.getNoneIndent()
//         }
//     }
    
//     private fun hasBlockAncestor(): Boolean {
//         var current = myNode.treeParent
        
//         while (current != null) {
//             val currentText = current.text
//             if (currentText.contains("{")) {
//                 return true
//             }
//             current = current.treeParent
//         }
        
//         return false
//     }
    
//     private fun isInsideBlock(): Boolean {
//         // Simple approach: look at the node's text content and its ancestors
//         var current = myNode.treeParent
//         var braceDepth = 0
        
//         while (current != null) {
//             val currentText = current.text
            
//             // Count opening and closing braces in ancestor nodes
//             val openBraces = currentText.count { it == '{' }
//             val closeBraces = currentText.count { it == '}' }
            
//             // If we find an ancestor with more opening braces than closing braces,
//             // we're likely inside a block
//             if (openBraces > closeBraces) {
//                 braceDepth += (openBraces - closeBraces)
//             }
            
//             current = current.treeParent
//         }
        
//         // If we have positive brace depth, we're inside a block
//         return braceDepth > 0
//     }

//     override fun getSpacing(child1: Block?, child2: Block): Spacing? {
//         return spacingBuilder.getSpacing(this, child1, child2)
//     }


//     override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
//         val nodeText = myNode.text
        
//         return when {
//             // If this node contains an opening brace, indent its children
//             nodeText.contains("{") -> ChildAttributes(Indent.getNormalIndent(), null)
            
//             else -> ChildAttributes(Indent.getNoneIndent(), null)
//         }
//     }
// */

//     override fun isLeaf(): Boolean {
//         return myNode.firstChildNode == null
//     }    
// }