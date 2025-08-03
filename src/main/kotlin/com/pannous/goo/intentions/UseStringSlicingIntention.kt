package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class UseStringSlicingIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo string slicing methods"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        // Look for string slicing patterns like s[1:], s[:1], s[1:3]
        val slicingPattern = Pattern.compile(
            """(\w+)\[(\d*):(\d*)\]"""
        )
        
        return slicingPattern.matcher(line).find()
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        var replacement = line
        
        // s[1:] => s.from(1)
        val fromPattern = Pattern.compile("""(\w+)\[(\d+):\]""")
        val fromMatcher = fromPattern.matcher(replacement)
        if (fromMatcher.find()) {
            val str = fromMatcher.group(1)
            val start = fromMatcher.group(2)
            replacement = replacement.replace(fromMatcher.group(0), "$str.from($start)")
        }
        
        // s[:1] => s.to(1)
        val toPattern = Pattern.compile("""(\w+)\[:(\d+)\]""")
        val toMatcher = toPattern.matcher(replacement)
        if (toMatcher.find()) {
            val str = toMatcher.group(1)
            val end = toMatcher.group(2)
            replacement = replacement.replace(toMatcher.group(0), "$str.to($end)")
        }
        
        // s[1:3] => s.sub(1, 3)
        val subPattern = Pattern.compile("""(\w+)\[(\d+):(\d+)\]""")
        val subMatcher = subPattern.matcher(replacement)
        if (subMatcher.find()) {
            val str = subMatcher.group(1)
            val start = subMatcher.group(2)
            val end = subMatcher.group(3)
            replacement = replacement.replace(subMatcher.group(0), "$str.sub($start, $end)")
        }
        
        if (replacement != line) {
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