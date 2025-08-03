package com.pannous.goo.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

class UseStringMethodsIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getText(): String = "Use Goo string method syntax"
    
    override fun getFamilyName(): String = "Goo Simplifications"
    
    private val stringMethods = mapOf(
        // String case methods
        "strings.ToUpper" to "toUpper",
        "strings.ToLower" to "toLower",
        "strings.Title" to "title",
        
        // String query methods  
        "strings.Contains" to "contains",
        "strings.HasPrefix" to "startsWith",
        "strings.HasSuffix" to "endsWith",
        "strings.Index" to "indexOf",
        
        // String manipulation
        "strings.Replace" to "replace",
        "strings.TrimSpace" to "trim",
        "strings.Split" to "split",
        
        // String length
        "len" to "length",
        
        // String conversion
        "strconv.Atoi" to "toInt",
        "strconv.ParseFloat" to "toFloat"
    )
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val line = getLineContaining(element) ?: return false
        
        return stringMethods.keys.any { goMethod ->
            line.contains(goMethod)
        }
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        val document = editor.document
        val line = getLineContaining(element) ?: return
        val (lineStart, lineEnd) = getLineRange(element) ?: return
        
        var replacement = line
        
        // Handle different string method patterns
        stringMethods.forEach { (goMethod, gooMethod) ->
            when (goMethod) {
                // strings.ToUpper(s) => s.toUpper()
                "strings.ToUpper", "strings.ToLower", "strings.Title", "strings.TrimSpace" -> {
                    val pattern = Pattern.compile("""$goMethod\(([^)]+)\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val arg = matcher.group(1)
                        replacement = replacement.replace(matcher.group(0), "$arg.$gooMethod()")
                    }
                }
                
                // strings.Contains(s, substr) => s.contains(substr)
                "strings.Contains", "strings.HasPrefix", "strings.HasSuffix", "strings.Index" -> {
                    val pattern = Pattern.compile("""$goMethod\(([^,]+),\s*([^)]+)\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val str = matcher.group(1)
                        val arg = matcher.group(2)
                        replacement = replacement.replace(matcher.group(0), "$str.$gooMethod($arg)")
                    }
                }
                
                // strings.Replace(s, old, new, -1) => s.replace(old, new)
                "strings.Replace" -> {
                    val pattern = Pattern.compile("""strings\.Replace\(([^,]+),\s*([^,]+),\s*([^,]+),\s*-1\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val str = matcher.group(1)
                        val old = matcher.group(2)
                        val new = matcher.group(3)
                        replacement = replacement.replace(matcher.group(0), "$str.replace($old, $new)")
                    }
                }
                
                // strings.Split(s, sep) => s.split(sep)
                "strings.Split" -> {
                    val pattern = Pattern.compile("""strings\.Split\(([^,]+),\s*([^)]+)\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val str = matcher.group(1)
                        val sep = matcher.group(2)
                        replacement = replacement.replace(matcher.group(0), "$str.split($sep)")
                    }
                }
                
                // len(s) => s.length()
                "len" -> {
                    val pattern = Pattern.compile("""len\(([^)]+)\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val arg = matcher.group(1)
                        replacement = replacement.replace(matcher.group(0), "$arg.length()")
                    }
                }
                
                // strconv.Atoi(s) => s.toInt()
                "strconv.Atoi" -> {
                    val pattern = Pattern.compile("""strconv\.Atoi\(([^)]+)\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val str = matcher.group(1)
                        replacement = replacement.replace(matcher.group(0), "$str.toInt()")
                    }
                }
                
                // strconv.ParseFloat(s, 64) => s.toFloat()
                "strconv.ParseFloat" -> {
                    val pattern = Pattern.compile("""strconv\.ParseFloat\(([^,]+),\s*64\)""")
                    val matcher = pattern.matcher(replacement)
                    if (matcher.find()) {
                        val str = matcher.group(1)
                        replacement = replacement.replace(matcher.group(0), "$str.toFloat()")
                    }
                }
            }
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