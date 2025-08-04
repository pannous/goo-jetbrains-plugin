package com.pannous.goo.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.util.ArrayUtil

abstract class GooElement(node: ASTNode) : ASTWrapperPsiElement(node)

class GooFunctionDeclaration(node: ASTNode) : GooElement(node), PsiNamedElement {
    override fun getName(): String? {
        // Find the identifier after func/def keyword
        val children = this.children
        for (i in children.indices) {
            if (children[i].text in listOf("func", "def") && i + 1 < children.size) {
                return children[i + 1].text
            }
        }
        return null
    }
    
    override fun setName(name: String): PsiElement {
        // For now, return this element as-is
        return this
    }
}

class GooVariableDeclaration(node: ASTNode) : GooElement(node), PsiNamedElement {
    override fun getName(): String? {
        // First identifier is the variable name
        return this.children.firstOrNull()?.text
    }
    
    override fun setName(name: String): PsiElement {
        return this
    }
}

class GooAssignment(node: ASTNode) : GooElement(node)

class GooIdentifier(node: ASTNode) : GooElement(node) {
    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }
    
    override fun getReference(): PsiReference? {
        val references = references
        return if (references.isNotEmpty()) references[0] else null
    }
}