package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class UseStringCaseIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo string case methods"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        val casePatterns = listOf(
            "strings.ToUpper", "strings.ToLower", "strings.Title",
            "unicode.ToUpper", "unicode.ToLower", "unicode.ToTitle"
        )
        
        return casePatterns.any { pattern -> line.contains(pattern) }
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        var replacement = line
        
        // strings.ToUpper(s) => s.upper() / s.toUpper() / s.upperCase()
        val upperPattern = Pattern.compile("""strings\.ToUpper\(([^)]+)\)""")
        val upperMatcher = upperPattern.matcher(replacement)
        if (upperMatcher.find()) {
            val str = upperMatcher.group(1)
            replacement = replacement.replace(upperMatcher.group(0), "$str.upper()")
        }
        
        // strings.ToLower(s) => s.lower() / s.toLower() / s.lowerCase()
        val lowerPattern = Pattern.compile("""strings\.ToLower\(([^)]+)\)""")
        val lowerMatcher = lowerPattern.matcher(replacement)
        if (lowerMatcher.find()) {
            val str = lowerMatcher.group(1)
            replacement = replacement.replace(lowerMatcher.group(0), "$str.lower()")
        }
        
        // strings.Title(s) => s.title() / s.capitalize()
        val titlePattern = Pattern.compile("""strings\.Title\(([^)]+)\)""")
        val titleMatcher = titlePattern.matcher(replacement)
        if (titleMatcher.find()) {
            val str = titleMatcher.group(1)
            replacement = replacement.replace(titleMatcher.group(0), "$str.capitalize()")
        }
        
        // Handle unicode package too
        val unicodeUpperPattern = Pattern.compile("""unicode\.ToUpper\(([^)]+)\)""")
        val unicodeUpperMatcher = unicodeUpperPattern.matcher(replacement)
        if (unicodeUpperMatcher.find()) {
            val str = unicodeUpperMatcher.group(1)
            replacement = replacement.replace(unicodeUpperMatcher.group(0), "$str.upper()")
        }
        
        val unicodeLowerPattern = Pattern.compile("""unicode\.ToLower\(([^)]+)\)""")
        val unicodeLowerMatcher = unicodeLowerPattern.matcher(replacement)
        if (unicodeLowerMatcher.find()) {
            val str = unicodeLowerMatcher.group(1)
            replacement = replacement.replace(unicodeLowerMatcher.group(0), "$str.lower()")
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