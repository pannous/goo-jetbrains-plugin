package com.pannous.goo.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.pannous.goo.psi.GooFile
import com.pannous.goo.lexer.GooTokenTypes

/**
 * Simple go-to-declaration handler that finds function and variable declarations
 * within the same file using text matching.
 */
class GooGotoDeclarationHandler : GotoDeclarationHandler {
    
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        
        if (sourceElement == null || sourceElement.containingFile !is GooFile) {
            return null
        }
        
        // Only handle identifiers
        if (sourceElement.node?.elementType != GooTokenTypes.IDENTIFIER) {
            return null
        }
        
        val identifierText = sourceElement.text
        if (identifierText.isBlank()) {
            return null
        }
        
        // Find declarations in the same file
        val file = sourceElement.containingFile
        val targets = mutableListOf<PsiElement>()
        
        // Simple text-based search for function declarations
        val fileText = file.text
        val lines = fileText.lines()
        
        for ((lineIndex, line) in lines.withIndex()) {
            // Look for function declarations: "func functionName" or "def functionName"
            val funcRegex = Regex("\\b(func|def)\\s+($identifierText)\\b")
            val funcMatch = funcRegex.find(line)
            if (funcMatch != null) {
                // Find the PSI element at this position
                val lineStartOffset = lines.take(lineIndex).sumOf { it.length + 1 }
                val functionNameOffset = lineStartOffset + funcMatch.range.first + funcMatch.groupValues[1].length + 1
                val target = file.findElementAt(functionNameOffset)
                if (target != null && target.text == identifierText) {
                    targets.add(target)
                }
            }
            
            // Look for variable declarations: "variableName :="
            val varRegex = Regex("\\b($identifierText)\\s*:=")
            val varMatch = varRegex.find(line)
            if (varMatch != null) {
                val lineStartOffset = lines.take(lineIndex).sumOf { it.length + 1 }
                val variableNameOffset = lineStartOffset + varMatch.range.first
                val target = file.findElementAt(variableNameOffset)
                if (target != null && target.text == identifierText) {
                    targets.add(target)
                }
            }
        }
        
        return if (targets.isNotEmpty()) targets.toTypedArray() else null
    }
}