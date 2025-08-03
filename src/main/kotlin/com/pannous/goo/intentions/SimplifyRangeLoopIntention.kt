package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class SimplifyRangeLoopIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo 'for x in xs' syntax"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        // Pattern: for i, x := range xs  OR  for _, x := range xs
        val pattern = Pattern.compile(
            """for\s+[_\w]+,\s*(\w+)\s*:=\s*range\s+(\w+)""",
            Pattern.DOTALL
        )
        
        return pattern.matcher(line.trim()).find()
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        // Pattern: for i, x := range xs  =>  for x in xs
        val pattern = Pattern.compile(
            """for\s+[_\w]+,\s*(\w+)\s*:=\s*range\s+(\w+)(.*)""",
            Pattern.DOTALL
        )
        
        val matcher = pattern.matcher(line.trim())
        if (matcher.find()) {
            val valueVar = matcher.group(1)
            val collection = matcher.group(2)
            val rest = matcher.group(3)
            
            val replacement = line.replace(matcher.group(0), "for $valueVar in $collection$rest")
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