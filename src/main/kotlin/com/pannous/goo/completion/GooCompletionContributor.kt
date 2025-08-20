package com.pannous.goo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes

class GooCompletionContributor : CompletionContributor() {
    
    init {
        // Built-in methods completion (e.g., "hello".reverse(), [1,2,3].filter()) - HIGHEST PRIORITY
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(GooLanguage)
                .afterLeaf(
                    PlatformPatterns.psiElement()
                        .withText(".")
                ),
            GooBuiltinMethodsProvider()
        )
        
        // Package member completion (e.g., units.Meter, fmt.Println) - HIGH PRIORITY
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(GooLanguage)
                .afterLeaf(
                    PlatformPatterns.psiElement()
                        .withText(".")
                ),
            GooPackageCompletionProvider()
        )
        
        // Direct package completion as fallback - HIGH PRIORITY
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(GooLanguage),
            GooDirectCompletionProvider()
        )
        
        // Basic keyword completion - LOWER PRIORITY
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(GooLanguage),
            GooKeywordCompletionProvider()
        )
        
        // Smart completion for more advanced features
        extend(
            CompletionType.SMART,
            PlatformPatterns.psiElement().withLanguage(GooLanguage),
            GooSmartCompletionProvider()
        )
    }
}

class GooKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    // Core language keywords from the lexer
    private val coreKeywords = listOf(
        "and", "or", "not", "def", "void", "func", "if", "else", "for", "while", 
        "return", "break", "continue", "package", "import", "var", "const",
        "printf", "check", "typeof", "class", "enum", "try", "catch", "put",
        "in", "is", "apply"
    )
    
    // Goo-specific symbols and operators
    private val gooSymbols = listOf(
        "ø"  // nil keyword
    )
    
    // Common Go keywords that work in Goo
    private val goKeywords = listOf(
        "interface", "struct", "map", "chan", "go", "defer", "select", "case", "default",
        "switch", "fallthrough", "type", "range", "nil", "true", "false"
    )
    
    // Built-in functions and common patterns
    private val builtinFunctions = listOf(
        "len", "cap", "make", "new", "append", "copy", "delete",
        "panic", "recover", "close", "real", "imag", "complex"
    )
    
    // Common import paths
    private val commonImports = listOf(
        "fmt", "os", "io", "strings", "strconv", "time", "sync", "net/http",
        "encoding/json", "github.com/veandco/go-sdl2/sdl"
    )
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        // Add core Goo keywords
        coreKeywords.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .withTypeText("Goo keyword")
                    .withBoldness(true)
            )
        }
        
        // Add Goo symbols
        gooSymbols.forEach { symbol ->
            result.addElement(
                LookupElementBuilder.create(symbol)
                    .withTypeText("Goo nil")
                    .withBoldness(true)
            )
        }
        
        // Add Go keywords that work in Goo
        goKeywords.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .withTypeText("Go keyword")
                    .withBoldness(true)
            )
        }
        
        // Add built-in functions
        builtinFunctions.forEach { func ->
            result.addElement(
                LookupElementBuilder.create(func)
                    .withTypeText("built-in function")
                    .withPresentableText("$func()")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val caretOffset = editor.caretModel.offset
                        editor.document.insertString(caretOffset, "()")
                        editor.caretModel.moveToOffset(caretOffset + 1)
                    }
            )
        }
        
        // Context-aware import suggestions
        val position = parameters.position
        val elementText = position.text
        val prevText = if (position.textOffset > 0) {
            try {
                val start = maxOf(0, position.textOffset - 20)
                parameters.originalFile.text.substring(start, position.textOffset).lowercase()
            } catch (e: Exception) { "" }
        } else ""
        
        // If we're after "import" keyword, suggest common imports
        if (prevText.contains("import") && !prevText.contains("\"")) {
            commonImports.forEach { importPath ->
                result.addElement(
                    LookupElementBuilder.create("\"$importPath\"")
                        .withTypeText("common import")
                        .withPresentableText(importPath)
                )
            }
        }
    }
}

class GooSmartCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val file = parameters.originalFile
        val editor = parameters.editor
        val project = parameters.position.project
        
        // Get surrounding text for context analysis
        val surroundingText = getSurroundingText(file, position.textOffset, 50)
        val lineText = getCurrentLineText(editor, position.textOffset)
        
        // Provide context-aware completions
        when {
            // After "import" keyword
            surroundingText.matches(Regex(".*import\\s*$", RegexOption.IGNORE_CASE)) -> {
                addImportCompletions(result)
            }
            
            // After package names (e.g., "fmt.")
            lineText.matches(Regex(".*\\b\\w+\\.$")) -> {
                addPackageMemberCompletions(result, lineText)
            }
            
            // Inside function calls
            surroundingText.contains("(") && !surroundingText.contains(")") -> {
                addFunctionParameterCompletions(result)
            }
            
            // String method completion (e.g., "text".)
            lineText.matches(Regex(".*\"[^\"]*\"\\.$")) -> {
                addStringMethodCompletions(result)
            }
            
            // Array/slice completion (e.g., [1,2,3].)
            lineText.matches(Regex(".*\\][^\\[]*\\.$")) -> {
                addArrayMethodCompletions(result)
            }
            
            // After control flow keywords
            surroundingText.matches(Regex(".*(if|while|for)\\s*$", RegexOption.IGNORE_CASE)) -> {
                addControlFlowCompletions(result)
            }
        }
    }
    
    private fun getSurroundingText(file: com.intellij.psi.PsiFile, offset: Int, radius: Int): String {
        return try {
            val start = maxOf(0, offset - radius)
            val end = minOf(file.textLength, offset + radius)
            file.text.substring(start, end).lowercase()
        } catch (e: Exception) { "" }
    }
    
    private fun getCurrentLineText(editor: Editor?, offset: Int): String {
        return try {
            if (editor == null) return ""
            val document = editor.document
            val line = document.getLineNumber(offset)
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = minOf(document.getLineEndOffset(line), offset)
            document.getText().substring(lineStart, lineEnd)
        } catch (e: Exception) { "" }
    }
    
    private fun addImportCompletions(result: CompletionResultSet) {
        val importSuggestions = listOf(
            "fmt" to "Formatted I/O functions",
            "os" to "Operating system interface", 
            "io" to "Basic I/O primitives",
            "strings" to "String manipulation functions",
            "strconv" to "String conversion functions",
            "time" to "Time manipulation functions",
            "net/http" to "HTTP client and server",
            "encoding/json" to "JSON encoding and decoding",
            "github.com/veandco/go-sdl2/sdl" to "SDL2 bindings"
        )
        
        importSuggestions.forEach { (path, description) ->
            result.addElement(
                LookupElementBuilder.create("\"$path\"")
                    .withTypeText(description)
                    .withPresentableText(path)
            )
        }
    }
    
    private fun addPackageMemberCompletions(result: CompletionResultSet, lineText: String) {
        val packageName = lineText.substringBeforeLast(".").substringAfterLast(" ").trim()
        
        // Note: This method is now supplemented by GooPackageCompletionProvider
        // which uses go doc for more comprehensive completion
        when (packageName) {
            "fmt" -> {
                listOf("Printf", "Println", "Print", "Sprintf", "Scanln", "Scanf").forEach { method ->
                    result.addElement(
                        LookupElementBuilder.create(method)
                            .withTypeText("fmt method")
                            .withInsertHandler { context, item ->
                                val editor = context.editor
                                val caretOffset = editor.caretModel.offset
                                editor.document.insertString(caretOffset, "()")
                                editor.caretModel.moveToOffset(caretOffset + 1)
                            }
                    )
                }
            }
            "os" -> {
                listOf("Exit", "Getenv", "Setenv", "Args", "Open", "Create").forEach { method ->
                    result.addElement(
                        LookupElementBuilder.create(method)
                            .withTypeText("os method")
                    )
                }
            }
            "strings" -> {
                listOf("Contains", "HasPrefix", "HasSuffix", "Split", "Join", "Replace", "ToLower", "ToUpper").forEach { method ->
                    result.addElement(
                        LookupElementBuilder.create(method)
                            .withTypeText("strings method")
                    )
                }
            }
            "units" -> {
                // Enhanced units package completion
                listOf(
                    "Meter", "Kilogram", "Second", "Ampere", "Kelvin", "Mole", "Candela",
                    "Newton", "Joule", "Watt", "Pascal", "Volt", "Ohm", "Farad",
                    "Convert", "String", "Parse", "New", "Scale"
                ).forEach { symbol ->
                    result.addElement(
                        LookupElementBuilder.create(symbol)
                            .withTypeText("units symbol")
                            .withInsertHandler { context, item ->
                                // Add parentheses for function-like symbols
                                if (symbol in listOf("Convert", "String", "Parse", "New", "Scale")) {
                                    val editor = context.editor
                                    val caretOffset = editor.caretModel.offset
                                    editor.document.insertString(caretOffset, "()")
                                    editor.caretModel.moveToOffset(caretOffset + 1)
                                }
                            }
                    )
                }
            }
            "sdl" -> {
                listOf("Init", "Quit", "CreateWindow", "GetMouseState", "PollEvent", "Delay").forEach { method ->
                    result.addElement(
                        LookupElementBuilder.create(method)
                            .withTypeText("SDL method")
                    )
                }
            }
        }
    }
    
    private fun addFunctionParameterCompletions(result: CompletionResultSet) {
        // Common parameter patterns
        listOf("true", "false", "nil", "ø", "\"\"", "0", "1", "-1").forEach { param ->
            result.addElement(
                LookupElementBuilder.create(param)
                    .withTypeText("common value")
            )
        }
    }
    
    private fun addStringMethodCompletions(result: CompletionResultSet) {
        // Note: Basic string methods are now handled by GooBuiltinMethodsProvider
        // This method is kept for backward compatibility and fallback cases
        listOf("length", "charAt", "substring").forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method)
                    .withTypeText("string method")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val caretOffset = editor.caretModel.offset
                        editor.document.insertString(caretOffset, "()")
                        editor.caretModel.moveToOffset(caretOffset + 1)
                    }
            )
        }
    }
    
    private fun addArrayMethodCompletions(result: CompletionResultSet) {
        // Array/slice methods from Goo
        listOf("apply", "filter", "length", "append").forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method)
                    .withTypeText("array method")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val caretOffset = editor.caretModel.offset  
                        editor.document.insertString(caretOffset, "()")
                        editor.caretModel.moveToOffset(caretOffset + 1)
                    }
            )
        }
    }
    
    private fun addControlFlowCompletions(result: CompletionResultSet) {
        // Common control flow patterns
        listOf(
            "true", "false", "not", "and", "or", "ø", 
            "len() > 0", "x != nil", "x ≠ ø"
        ).forEach { condition ->
            result.addElement(
                LookupElementBuilder.create(condition)
                    .withTypeText("condition")
            )
        }
    }
}