package com.pannous.goo.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement

class GooFoldingBuilder : FoldingBuilderEx() {
    
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        
        // Find multi-line comments and functions to fold
        collectFoldingRegions(root, document, descriptors)
        
        return descriptors.toTypedArray()
    }
    
    private fun collectFoldingRegions(element: PsiElement, document: Document, descriptors: MutableList<FoldingDescriptor>) {
        val text = element.text
        
        // Fold multi-line comments that start with #
        if (text.startsWith("#") && text.contains("\n")) {
            val lines = text.split("\n")
            if (lines.size > 1) {
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
        }
        
        // Fold function bodies (simple heuristic)
        if (text.contains("func ") || text.contains("def ") || text.contains("void ")) {
            val openBrace = text.indexOf("{")
            val closeBrace = text.lastIndexOf("}")
            if (openBrace != -1 && closeBrace != -1 && closeBrace > openBrace) {
                val startOffset = element.textRange.startOffset + openBrace
                val endOffset = element.textRange.startOffset + closeBrace + 1
                if (endOffset > startOffset + 2) {
                    descriptors.add(FoldingDescriptor(element.node, 
                        com.intellij.openapi.util.TextRange(startOffset, endOffset)))
                }
            }
        }
        
        // Recursively process children
        for (child in element.children) {
            collectFoldingRegions(child, document, descriptors)
        }
    }
    
    override fun getPlaceholderText(node: ASTNode): String {
        val text = node.text
        return when {
            text.startsWith("#") -> "# ..."
            text.contains("func ") -> "{ ... }"
            text.contains("def ") -> "{ ... }"
            text.contains("void ") -> "{ ... }"
            else -> "..."
        }
    }
    
    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}