package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class UseStringAccessIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo string access methods"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        // Look for string indexing like s[0], s[len(s)-1]
        return line.contains("[0]") || line.contains("[len(") || line.contains("[]rune(")
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        var replacement = line
        
        // s[0] => s.first()
        val firstPattern = Pattern.compile("""(\w+)\[0\]""")
        val firstMatcher = firstPattern.matcher(replacement)
        if (firstMatcher.find()) {
            val str = firstMatcher.group(1)
            replacement = replacement.replace(firstMatcher.group(0), "$str.first()")
        }
        
        // s[len(s)-1] => s.last()
        val lastPattern = Pattern.compile("""(\w+)\[len\(\1\)-1\]""")
        val lastMatcher = lastPattern.matcher(replacement)
        if (lastMatcher.find()) {
            val str = lastMatcher.group(1)
            replacement = replacement.replace(lastMatcher.group(0), "$str.last()")
        }
        
        // len(s) => s.size() / s.length()
        val lenPattern = Pattern.compile("""len\((\w+)\)""")
        val lenMatcher = lenPattern.matcher(replacement)
        if (lenMatcher.find()) {
            val str = lenMatcher.group(1)
            replacement = replacement.replace(lenMatcher.group(0), "$str.size()")
        }
        
        // []rune(s) => s.runes()
        val runesPattern = Pattern.compile("""\[\]rune\((\w+)\)""")
        val runesMatcher = runesPattern.matcher(replacement)
        if (runesMatcher.find()) {
            val str = runesMatcher.group(1)
            replacement = replacement.replace(runesMatcher.group(0), "$str.runes()")
        }
        
        // []byte(s) => s.bytes()
        val bytesPattern = Pattern.compile("""\[\]byte\((\w+)\)""")
        val bytesMatcher = bytesPattern.matcher(replacement)
        if (bytesMatcher.find()) {
            val str = bytesMatcher.group(1)
            replacement = replacement.replace(bytesMatcher.group(0), "$str.bytes()")
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