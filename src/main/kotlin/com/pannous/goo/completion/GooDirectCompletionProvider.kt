package com.pannous.goo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

/**
 * A simpler, more direct completion provider that checks for package.member patterns
 */
class GooDirectCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val file = parameters.originalFile
        
        try {
            val text = file.text
            val offset = position.textOffset
            
            // Get some context around the cursor
            val startOffset = maxOf(0, offset - 50)
            val endOffset = minOf(text.length, offset + 10)
            val contextText = text.substring(startOffset, endOffset)
            
            println("GooDirectCompletion: Context around cursor: '$contextText'")
            
            // Look for pattern: packageName.
            val beforeCursor = text.substring(0, offset)
            val dotIndex = beforeCursor.lastIndexOf('.')
            
            if (dotIndex > 0) {
                // Find the identifier before the dot
                val beforeDot = beforeCursor.substring(0, dotIndex).trim()
                val spaceIndex = maxOf(
                    beforeDot.lastIndexOf(' '),
                    beforeDot.lastIndexOf('\t'),
                    beforeDot.lastIndexOf('\n'),
                    beforeDot.lastIndexOf('='),
                    beforeDot.lastIndexOf('('),
                    beforeDot.lastIndexOf(':')
                )
                
                val identifier = beforeDot.substring(spaceIndex + 1).trim()
                println("GooDirectCompletion: Found identifier before dot: '$identifier'")
                
                if (identifier.isNotEmpty() && identifier.matches(Regex("\\w+"))) {
                    // Check if it's a known package and add completions
                    addPackageCompletions(result, identifier)
                }
            }
        } catch (e: Exception) {
            println("GooDirectCompletion: Error: ${e.message}")
        }
    }
    
    private fun addPackageCompletions(result: CompletionResultSet, packageName: String) {
        println("GooDirectCompletion: Adding completions for package: $packageName")
        
        val symbols = when (packageName) {
            "strings" -> listOf(
                "Contains", "HasPrefix", "HasSuffix", "Split", "Join", 
                "Replace", "ToLower", "ToUpper", "Trim", "Fields"
            )
            "fmt" -> listOf(
                "Printf", "Println", "Print", "Sprintf", "Scanln", "Scanf"
            )
            "os" -> listOf(
                "Exit", "Getenv", "Setenv", "Args", "Open", "Create", "Remove"
            )
            "units" -> listOf(
                "Meter", "Kilogram", "Second", "Ampere", "Kelvin", "Mole", "Candela",
                "Newton", "Joule", "Watt", "Pascal", "Volt", "Ohm", "Farad",
                "Convert", "String", "Parse"
            )
            else -> {
                println("GooDirectCompletion: Unknown package: $packageName")
                return
            }
        }
        
        println("GooDirectCompletion: Adding ${symbols.size} symbols")
        
        symbols.forEach { symbol ->
            val element = LookupElementBuilder.create(symbol)
                .withTypeText("$packageName member")
                .withInsertHandler { context, item ->
                    // For functions, add parentheses
                    val functionalSymbols = setOf("Contains", "HasPrefix", "HasSuffix", "Split", "Join", 
                        "Replace", "ToLower", "ToUpper", "Trim", "Fields", "Printf", "Println", 
                        "Print", "Sprintf", "Scanln", "Scanf", "Exit", "Getenv", "Setenv", 
                        "Open", "Create", "Remove", "Convert", "String", "Parse")
                    
                    if (symbol in functionalSymbols) {
                        val editor = context.editor
                        val caretOffset = editor.caretModel.offset
                        editor.document.insertString(caretOffset, "()")
                        editor.caretModel.moveToOffset(caretOffset + 1)
                    }
                }
            
            result.addElement(element)
        }
    }
}