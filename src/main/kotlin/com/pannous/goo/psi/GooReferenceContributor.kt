package com.pannous.goo.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.pannous.goo.lexer.GooTokenTypes

class GooReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Register references for identifiers
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withElementType(GooTokenTypes.IDENTIFIER),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element !is GooIdentifier) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    
                    return arrayOf(GooReference(element))
                }
            }
        )
    }
}

class GooReference(private val element: GooIdentifier) : PsiReferenceBase<GooIdentifier>(
    element,
    TextRange(0, element.textLength)
) {
    
    override fun resolve(): PsiElement? {
        val name = element.text
        
        // Look for function declarations in the same file
        val file = element.containingFile
        val functions = PsiTreeUtil.findChildrenOfType(file, GooFunctionDeclaration::class.java)
        
        for (function in functions) {
            if (function.name == name) {
                return function
            }
        }
        
        // Look for variable declarations in the same file
        val variables = PsiTreeUtil.findChildrenOfType(file, GooVariableDeclaration::class.java)
        for (variable in variables) {
            if (variable.name == name) {
                return variable
            }
        }
        
        return null
    }
    
    override fun getVariants(): Array<Any> {
        // Return completion variants
        val file = element.containingFile
        val variants = mutableListOf<String>()
        
        // Add function names
        val functions = PsiTreeUtil.findChildrenOfType(file, GooFunctionDeclaration::class.java)
        functions.mapNotNull { it.name }.forEach { variants.add(it) }
        
        // Add variable names
        val variables = PsiTreeUtil.findChildrenOfType(file, GooVariableDeclaration::class.java)
        variables.mapNotNull { it.name }.forEach { variants.add(it) }
        
        return variants.toTypedArray()
    }
}