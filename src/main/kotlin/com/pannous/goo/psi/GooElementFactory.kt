package com.pannous.goo.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

object GooElementFactory {
    fun createElement(node: ASTNode): PsiElement {
        // Simple leaf elements only - no custom PSI to avoid crashes
        return LeafPsiElement(node.elementType, node.text)
    }
}