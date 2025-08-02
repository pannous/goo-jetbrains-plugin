package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class SimplifyFunctionReturnIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Remove explicit return for single expression"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val elementText = element.containingFile?.text ?: return false
        val functionBlock = getFunctionBlock(element) ?: return false
        
        // Pattern: func name() type { return expression }
        val pattern = Pattern.compile(
            """func\s+\w+\([^)]*\)\s*\w*\s*\{\s*return\s+([^}]+)\s*\}""",
            Pattern.DOTALL
        )
        
        return pattern.matcher(functionBlock).find()
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val functionBlock = getFunctionBlock(element) ?: return
        val (start, end) = getFunctionBlockRange(element) ?: return
        
        val pattern = Pattern.compile(
            """(func\s+\w+\([^)]*\)\s*\w*\s*\{\s*)return\s+([^}]+)(\s*\})""",
            Pattern.DOTALL
        )
        
        val matcher = pattern.matcher(functionBlock)
        if (matcher.find()) {
            val prefix = matcher.group(1)
            val expression = matcher.group(2).trim()
            val suffix = matcher.group(3)
            
            val replacement = "$prefix$expression$suffix"
            document.replaceString(start, end, replacement)
        }
    }
    
    private fun getFunctionBlock(element: PsiElement): String? {
        val text = element.containingFile.text
        val offset = element.textOffset
        
        // Find function start by looking backwards for "func"
        var start = offset
        while (start > 0) {
            val substr = text.substring(maxOf(0, start - 4), start + 1)
            if (substr.contains("func")) {
                start = text.lastIndexOf("func", start)
                break
            }
            start--
        }
        
        if (start <= 0) return null
        
        // Find matching closing brace
        var braceCount = 0
        var end = start
        var inFunction = false
        
        while (end < text.length) {
            when (text[end]) {
                '{' -> {
                    inFunction = true
                    braceCount++
                }
                '}' -> {
                    braceCount--
                    if (inFunction && braceCount == 0) {
                        end++
                        break
                    }
                }
            }
            end++
        }
        
        return if (inFunction && braceCount == 0) {
            text.substring(start, end)
        } else null
    }
    
    private fun getFunctionBlockRange(element: PsiElement): Pair<Int, Int>? {
        val text = element.containingFile.text
        val offset = element.textOffset
        
        // Find function start
        var start = offset
        while (start > 0) {
            val substr = text.substring(maxOf(0, start - 4), start + 1)
            if (substr.contains("func")) {
                start = text.lastIndexOf("func", start)
                break
            }
            start--
        }
        
        if (start <= 0) return null
        
        // Find matching closing brace
        var braceCount = 0
        var end = start
        var inFunction = false
        
        while (end < text.length) {
            when (text[end]) {
                '{' -> {
                    inFunction = true
                    braceCount++
                }
                '}' -> {
                    braceCount--
                    if (inFunction && braceCount == 0) {
                        end++
                        break
                    }
                }
            }
            end++
        }
        
        return if (inFunction && braceCount == 0) {
            Pair(start, end)
        } else null
    }
}