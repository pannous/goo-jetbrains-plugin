package com.pannous.goo.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages

class GooIntroduceVariableHandler : RefactoringActionHandler {
    
    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext?) {
        val selectionModel = editor.selectionModel
        
        if (!selectionModel.hasSelection()) {
            // No selection - try to select expression under cursor
            val offset = editor.caretModel.offset
            selectExpressionAtOffset(editor, file, offset)
        }
        
        if (selectionModel.hasSelection()) {
            val selectedText = selectionModel.selectedText
            if (selectedText != null && selectedText.isNotBlank()) {
                introduceVariable(project, editor, file, selectedText)
            } else {
                Messages.showErrorDialog(project, "Please select an expression to extract", "Introduce Variable")
            }
        } else {
            Messages.showErrorDialog(project, "Please select an expression to extract", "Introduce Variable")
        }
    }
    
    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // Not used for introduce variable
    }
    
    private fun selectExpressionAtOffset(editor: Editor, file: PsiFile, offset: Int) {
        val text = file.text
        
        // Find expression boundaries - look for common Goo expressions
        var start = offset
        var end = offset
        
        // Expand backwards to find start of expression
        while (start > 0) {
            val char = text[start - 1]
            if (char.isWhitespace() || char in "=,;(){}\n") {
                break
            }
            start--
        }
        
        // Expand forwards to find end of expression  
        while (end < text.length) {
            val char = text[end]
            if (char.isWhitespace() || char in ",;(){}\n") {
                break
            }
            end++
        }
        
        // Select the expression if we found something meaningful
        if (end > start && end - start > 1) {
            editor.selectionModel.setSelection(start, end)
        }
    }
    
    private fun introduceVariable(project: Project, editor: Editor, file: PsiFile, selectedText: String) {
        val selectionModel = editor.selectionModel
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        
        // Generate a variable name based on the expression
        val suggestedName = generateVariableName(selectedText)
        
        // Ask user for variable name
        val variableName = Messages.showInputDialog(
            project,
            "Enter variable name:",
            "Introduce Variable",
            Messages.getQuestionIcon(),
            suggestedName,
            null
        ) ?: return
        
        if (variableName.isBlank()) {
            Messages.showErrorDialog(project, "Variable name cannot be empty", "Introduce Variable")
            return
        }
        
        WriteCommandAction.runWriteCommandAction(project, "Introduce Variable", null, {
            val document = editor.document
            
            // Find the best place to insert the variable declaration
            val insertOffset = findInsertionPoint(file.text, startOffset)
            
            // Determine indentation
            val lineStart = document.text.lastIndexOf('\n', insertOffset) + 1
            val lineText = document.text.substring(lineStart, insertOffset)
            val indentation = lineText.takeWhile { it.isWhitespace() }
            
            // Create variable declaration in Goo style
            val variableDeclaration = "$indentation$variableName := $selectedText\n"
            
            // Insert the variable declaration
            document.insertString(insertOffset, variableDeclaration)
            
            // Replace the selected expression with the variable name
            val newStartOffset = startOffset + variableDeclaration.length
            val newEndOffset = endOffset + variableDeclaration.length
            document.replaceString(newStartOffset, newEndOffset, variableName)
            
            // Position cursor after the replacement
            editor.caretModel.moveToOffset(newStartOffset + variableName.length)
        }, file)
    }
    
    private fun generateVariableName(expression: String): String {
        val cleaned = expression.trim()
        
        return when {
            // Goo-specific patterns
            cleaned.contains(".upper()") || cleaned.contains(".toUpper()") -> "upperStr"
            cleaned.contains(".lower()") || cleaned.contains(".toLower()") -> "lowerStr"
            cleaned.contains(".capitalize()") || cleaned.contains(".title()") -> "titleStr"
            cleaned.contains(".trim()") -> "trimmedStr"
            cleaned.contains(".replace(") -> "replacedStr"
            cleaned.contains(".split(") -> "parts"
            cleaned.contains(".join(") -> "joinedStr"
            cleaned.contains(".contains(") -> "hasMatch"
            cleaned.contains(".startsWith(") -> "startsWithMatch"
            cleaned.contains(".endsWith(") -> "endsWithMatch"
            cleaned.contains(".indexOf(") -> "index"
            cleaned.contains(".length()") || cleaned.contains(".size()") -> "length"
            cleaned.contains(".first()") -> "firstChar"
            cleaned.contains(".last()") -> "lastChar"
            cleaned.contains(".from(") -> "substring"
            cleaned.contains(".to(") -> "prefix"
            cleaned.contains(".sub(") -> "substring"
            cleaned.contains(".bytes()") -> "bytes"
            cleaned.contains(".runes()") -> "runes"
            cleaned.contains(".toInt()") -> "intValue"
            cleaned.contains(".toFloat()") -> "floatValue"
            
            // Array/slice operations
            cleaned.contains("#") && !cleaned.startsWith("#") -> "element"
            cleaned.contains("[") && cleaned.contains("]") -> "item"
            
            // Try operations
            cleaned.startsWith("try ") -> "result"
            
            // String literals
            cleaned.startsWith("\"") && cleaned.endsWith("\"") -> "str"
            cleaned.startsWith("'") && cleaned.endsWith("'") -> "char"
            
            // Numbers
            cleaned.matches(Regex("""\d+""")) -> "num"
            cleaned.matches(Regex("""\d+\.\d+""")) -> "value"
            
            // Method calls
            cleaned.contains(".") -> {
                val methodName = cleaned.substringAfterLast(".")
                    .substringBefore("(")
                    .takeIf { it.isNotEmpty() } ?: "result"
                methodName
            }
            
            // Function calls
            cleaned.contains("(") -> {
                val funcName = cleaned.substringBefore("(")
                    .takeIf { it.isNotEmpty() } ?: "result"
                when (funcName) {
                    "printf", "print", "println" -> "output"
                    "check" -> "assertion"
                    "typeof" -> "type"
                    else -> funcName + "Result"
                }
            }
            
            // Operators
            cleaned.contains(" + ") -> "sum"
            cleaned.contains(" - ") -> "diff"
            cleaned.contains(" * ") -> "product"
            cleaned.contains(" / ") -> "quotient"
            cleaned.contains(" % ") -> "remainder"
            cleaned.contains(" and ") || cleaned.contains(" && ") -> "condition"
            cleaned.contains(" or ") || cleaned.contains(" || ") -> "condition"
            cleaned.contains(" not ") || cleaned.contains(" ¬ ") -> "negated"
            cleaned.contains(" == ") -> "isEqual"
            cleaned.contains(" ≠ ") || cleaned.contains(" != ") -> "isNotEqual"
            cleaned.contains(" < ") -> "isLess"
            cleaned.contains(" > ") -> "isGreater"
            cleaned.contains(" <= ") -> "isLessOrEqual"
            cleaned.contains(" >= ") -> "isGreaterOrEqual"
            
            // Collections
            cleaned.startsWith("[") && cleaned.endsWith("]") -> "list"
            cleaned.startsWith("{") && cleaned.endsWith("}") -> "map"
            
            // Default based on first word
            else -> {
                val firstWord = cleaned.split(Regex("""[\s\[\]().,]""")).firstOrNull()
                when (firstWord) {
                    "ø", "nil" -> "nullValue"
                    else -> "value"
                }
            }
        }
    }
    
    private fun findInsertionPoint(text: String, currentOffset: Int): Int {
        // Find the start of the current statement/line
        var offset = currentOffset
        
        // Go backwards to find a good insertion point (start of line or after {)
        while (offset > 0) {
            val char = text[offset - 1]
            when (char) {
                '\n' -> {
                    // Found start of line - this is a good insertion point
                    return offset
                }
                '{' -> {
                    // Found opening brace - insert after it with newline
                    return offset
                }
                ';' -> {
                    // Found statement end - could insert after this
                    val nextNewline = text.indexOf('\n', offset)
                    if (nextNewline != -1) {
                        return nextNewline + 1
                    }
                }
            }
            offset--
        }
        
        // Fallback to current position
        return currentOffset
    }
}