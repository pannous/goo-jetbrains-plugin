package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import java.util.regex.Pattern

class SimplifyErrorHandlingIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Simplify to 'try' statement"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val elementText = element.containingFile?.text ?: return false
        val line = getLineContaining(element) ?: return false
        
        // Pattern: if err := f(); err != nil { return err }
        val pattern = Pattern.compile(
            """if\s+err\s*:=\s*([^;]+);\s*err\s*!=\s*nil\s*\{\s*return\s+err\s*\}""",
            Pattern.DOTALL
        )
        
        return pattern.matcher(line.trim()).find()
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val lineStart = getLineStart(element, document.text)
        val lineEnd = getLineEnd(element, document.text)
        
        val pattern = Pattern.compile(
            """if\s+err\s*:=\s*([^;]+);\s*err\s*!=\s*nil\s*\{\s*return\s+err\s*\}""",
            Pattern.DOTALL
        )
        
        val matcher = pattern.matcher(line.trim())
        if (matcher.find()) {
            val functionCall = matcher.group(1).trim()
            val replacement = "try $functionCall"
            
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
    
    private fun getLineStart(element: PsiElement, text: String): Int {
        val offset = element.textOffset
        return text.lastIndexOf('\n', offset - 1) + 1
    }
    
    private fun getLineEnd(element: PsiElement, text: String): Int {
        val offset = element.textOffset
        val lineEnd = text.indexOf('\n', offset)
        return if (lineEnd == -1) text.length else lineEnd
    }
}