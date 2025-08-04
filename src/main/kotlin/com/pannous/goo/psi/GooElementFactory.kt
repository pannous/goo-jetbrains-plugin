package com.pannous.goo.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

object GooElementFactory {
    fun createElement(node: ASTNode): PsiElement {
        // For now, create simple leaf elements to avoid crashes
        // This can be enhanced later with specific PSI element types
        return when (node.elementType) {
            GooElementTypes.FUNCTION_DECLARATION -> GooFunctionDeclaration(node)
            GooElementTypes.VARIABLE_DECLARATION -> GooVariableDeclaration(node)
            GooElementTypes.ASSIGNMENT -> GooAssignment(node)
            GooElementTypes.IDENTIFIER -> GooIdentifier(node)
            else -> LeafPsiElement(node.elementType, node.text)
        }
    }
}