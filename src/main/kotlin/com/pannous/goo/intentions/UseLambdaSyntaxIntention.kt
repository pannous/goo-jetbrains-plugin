package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class UseLambdaSyntaxIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo lambda syntax"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        // Pattern: func(param type) returnType { return expression }
        val pattern = Pattern.compile(
            """func\s*\(\s*(\w+)\s+\w+\s*\)\s*\w+\s*\{\s*return\s+([^}]+)\s*\}""",
            Pattern.DOTALL
        )
        
        return pattern.matcher(line).find()
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        // Pattern: func(param type) returnType { return expression }
        val pattern = Pattern.compile(
            """func\s*\(\s*(\w+)\s+\w+\s*\)\s*\w+\s*\{\s*return\s+([^}]+)\s*\}""",
            Pattern.DOTALL
        )
        
        val matcher = pattern.matcher(line)
        if (matcher.find()) {
            val paramName = matcher.group(1).trim()
            val expression = matcher.group(2).trim()
            
            val replacement = line.replace(matcher.group(0), "$paramName => $expression")
            document.replaceString(lineStart, lineEnd, replacement)
        }
    }
    
    private fun getLineContaining(element: PsiElement): String? {
        val file = element.containingFile
        val text = file.text
        val offset = element.textOffset
        
        val lineStart = text.lastIndexOf('\n', offset - 1) + 1
        val lineEnd = text.indexOf('\n', offset)
        val actualEnd = if (lineEnd == -1) text.length else lineEnd
        
        return text.substring(lineStart, actualEnd)
    }
    
    private fun getLineRange(element: PsiElement): Pair<Int, Int>? {
        val text = element.containingFile.text
        val offset = element.textOffset
        
        val lineStart = text.lastIndexOf('\n', offset - 1) + 1
        val lineEnd = text.indexOf('\n', offset)
        val actualEnd = if (lineEnd == -1) text.length else lineEnd
        
        return Pair(lineStart, actualEnd)
    }
}