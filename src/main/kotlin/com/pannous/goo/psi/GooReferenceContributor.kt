package com.pannous.goo.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.pannous.goo.lexer.GooTokenTypes
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.openapi.project.Project

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
        val file = element.containingFile
        
        // Special handling for Goo packages
        if (name in setOf("units")) {
            return createGooPackageElement(name)
        }
        
        // Special handling for units package members
        val unitMembers = setOf("Meter", "Kilogram", "Second", "Ampere", "Kelvin", "Mole", "Candela",
                               "Newton", "Joule", "Watt", "Pascal", "Volt", "Ohm", "Farad",
                               "Convert", "String", "Parse", "New", "Scale")
        if (name in unitMembers) {
            val prevSibling = element.prevSibling
            if (prevSibling?.text == ".") {
                val packageElement = prevSibling.prevSibling
                if (packageElement?.text == "units") {
                    return createGooPackageMemberElement("units", name)
                }
            }
        }
        
        // Look for function declarations in the same file
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
    
    private fun createGooPackageElement(packageName: String): PsiElement {
        return object : PsiElement by element {
            override fun toString(): String = "Goo Package: $packageName"
            override fun getText(): String = when (packageName) {
                "units" -> "// Package units provides physical unit definitions and conversions"
                else -> "// Goo package: $packageName"
            }
        }
    }
    
    private fun createGooPackageMemberElement(packageName: String, memberName: String): PsiElement {
        return object : PsiElement by element {
            override fun toString(): String = "$packageName.$memberName"
            override fun getText(): String = when (packageName) {
                "units" -> when (memberName) {
                    "Meter" -> "const Meter Unit // Length unit (m)"
                    "Kilogram" -> "const Kilogram Unit // Mass unit (kg)"
                    "Second" -> "const Second Unit // Time unit (s)"
                    "Newton" -> "const Newton Unit // Force unit (N)"
                    "Joule" -> "const Joule Unit // Energy unit (J)"
                    "Convert" -> "func Convert(value float64, from, to Unit) float64"
                    "String" -> "func String(unit Unit) string"
                    "Parse" -> "func Parse(s string) (Unit, error)"
                    else -> "$memberName // $packageName member"
                }
                else -> "$memberName // $packageName member"
            }
        }
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