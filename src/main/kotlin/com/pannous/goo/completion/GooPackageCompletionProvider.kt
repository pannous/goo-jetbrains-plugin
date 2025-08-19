package com.pannous.goo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import com.pannous.goo.services.GooCompilerService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class GooPackageCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    companion object {
        // Cache for package symbols to avoid repeated go doc calls
        private val symbolCache = ConcurrentHashMap<String, List<PackageSymbol>>()
        private val cacheTimestamp = ConcurrentHashMap<String, Long>()
        private const val CACHE_DURATION_MS = 30000 // 30 seconds
    }
    
    data class PackageSymbol(
        val name: String,
        val type: SymbolType,
        val signature: String = "",
        val documentation: String = ""
    )
    
    enum class SymbolType {
        FUNCTION, VARIABLE, CONSTANT, TYPE, METHOD
    }
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val file = parameters.originalFile
        val project = parameters.position.project
        
        // Extract package name from context
        val packageName = extractPackageNameFromContext(position, file)
        if (packageName != null) {
            val symbols = getPackageSymbols(packageName, project)
            addSymbolCompletions(result, symbols, packageName)
        }
    }
    
    private fun extractPackageNameFromContext(position: PsiElement, file: PsiFile): String? {
        try {
            val text = file.text
            val offset = position.textOffset
            
            // Look backwards from cursor to find package name before dot
            var i = offset - 1
            while (i >= 0 && text[i] == '.') i-- // Skip dots
            if (i < 0) return null
            
            // Find the end of the identifier
            val end = i + 1
            while (i >= 0 && (text[i].isLetterOrDigit() || text[i] == '_')) i--
            val start = i + 1
            
            if (start < end) {
                val identifier = text.substring(start, end)
                
                // Check if this identifier is an imported package
                if (isImportedPackage(identifier, file)) {
                    return identifier
                }
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }
    
    private fun isImportedPackage(identifier: String, file: PsiFile): Boolean {
        val text = file.text
        
        // Look for import statements
        val importRegex = Regex("""import\s+["']([^"']+)["']""")
        val matches = importRegex.findAll(text)
        
        for (match in matches) {
            val importPath = match.groupValues[1]
            val packageName = importPath.substringAfterLast('/')
            if (packageName == identifier) {
                return true
            }
        }
        
        // Also check for aliased imports
        val aliasRegex = Regex("""import\s+(\w+)\s+["']([^"']+)["']""")
        val aliasMatches = aliasRegex.findAll(text)
        
        for (match in aliasMatches) {
            val alias = match.groupValues[1]
            if (alias == identifier) {
                return true
            }
        }
        
        return false
    }
    
    private fun getPackageSymbols(packageName: String, project: Project): List<PackageSymbol> {
        val now = System.currentTimeMillis()
        
        // Check cache first
        val cachedTime = cacheTimestamp[packageName]
        if (cachedTime != null && (now - cachedTime) < CACHE_DURATION_MS) {
            symbolCache[packageName]?.let { return it }
        }
        
        // Get symbols from go doc
        val symbols = fetchPackageSymbolsFromGoDocs(packageName, project)
        
        // Update cache
        if (symbols.isNotEmpty()) {
            symbolCache[packageName] = symbols
            cacheTimestamp[packageName] = now
        }
        
        return symbols
    }
    
    private fun fetchPackageSymbolsFromGoDocs(packageName: String, project: Project): List<PackageSymbol> {
        try {
            // First try to use go doc to get package information
            val symbols = mutableListOf<PackageSymbol>()
            
            // Get basic package info
            val goDocOutput = executeGoDoc(packageName)
            if (goDocOutput.isNotEmpty()) {
                symbols.addAll(parseGoDocOutput(goDocOutput))
            }
            
            // Fallback to hardcoded common packages if go doc fails
            if (symbols.isEmpty()) {
                symbols.addAll(getHardcodedPackageSymbols(packageName))
            }
            
            return symbols
            
        } catch (e: Exception) {
            // If go doc fails, return hardcoded symbols
            return getHardcodedPackageSymbols(packageName)
        }
    }
    
    private fun executeGoDoc(packageName: String): String {
        return try {
            val processBuilder = ProcessBuilder("go", "doc", packageName)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            
            // Wait for process to complete with timeout
            process.waitFor(5, TimeUnit.SECONDS)
            process.destroy()
            
            output
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun parseGoDocOutput(output: String): List<PackageSymbol> {
        val symbols = mutableListOf<PackageSymbol>()
        val lines = output.split('\n')
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                // Functions: func FunctionName(...)
                trimmedLine.startsWith("func ") && !trimmedLine.contains("(") -> {
                    val name = extractFunctionName(trimmedLine)
                    if (name != null) {
                        symbols.add(PackageSymbol(name, SymbolType.FUNCTION, trimmedLine))
                    }
                }
                
                // Variables: var VariableName
                trimmedLine.startsWith("var ") -> {
                    val name = extractVariableName(trimmedLine)
                    if (name != null) {
                        symbols.add(PackageSymbol(name, SymbolType.VARIABLE, trimmedLine))
                    }
                }
                
                // Constants: const ConstantName
                trimmedLine.startsWith("const ") -> {
                    val name = extractConstantName(trimmedLine)
                    if (name != null) {
                        symbols.add(PackageSymbol(name, SymbolType.CONSTANT, trimmedLine))
                    }
                }
                
                // Types: type TypeName
                trimmedLine.startsWith("type ") -> {
                    val name = extractTypeName(trimmedLine)
                    if (name != null) {
                        symbols.add(PackageSymbol(name, SymbolType.TYPE, trimmedLine))
                    }
                }
            }
        }
        
        return symbols
    }
    
    private fun extractFunctionName(line: String): String? {
        val regex = Regex("""func\s+(\w+)""")
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun extractVariableName(line: String): String? {
        val regex = Regex("""var\s+(\w+)""")
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun extractConstantName(line: String): String? {
        val regex = Regex("""const\s+(\w+)""")
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun extractTypeName(line: String): String? {
        val regex = Regex("""type\s+(\w+)""")
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun getHardcodedPackageSymbols(packageName: String): List<PackageSymbol> {
        return when (packageName) {
            "fmt" -> listOf(
                PackageSymbol("Printf", SymbolType.FUNCTION, "func Printf(format string, a ...interface{}) (n int, err error)"),
                PackageSymbol("Println", SymbolType.FUNCTION, "func Println(a ...interface{}) (n int, err error)"),
                PackageSymbol("Print", SymbolType.FUNCTION, "func Print(a ...interface{}) (n int, err error)"),
                PackageSymbol("Sprintf", SymbolType.FUNCTION, "func Sprintf(format string, a ...interface{}) string"),
                PackageSymbol("Scanln", SymbolType.FUNCTION, "func Scanln(a ...interface{}) (n int, err error)"),
                PackageSymbol("Scanf", SymbolType.FUNCTION, "func Scanf(format string, a ...interface{}) (n int, err error)")
            )
            
            "os" -> listOf(
                PackageSymbol("Exit", SymbolType.FUNCTION, "func Exit(code int)"),
                PackageSymbol("Getenv", SymbolType.FUNCTION, "func Getenv(key string) string"),
                PackageSymbol("Setenv", SymbolType.FUNCTION, "func Setenv(key, value string) error"),
                PackageSymbol("Args", SymbolType.VARIABLE, "var Args []string"),
                PackageSymbol("Open", SymbolType.FUNCTION, "func Open(name string) (*File, error)"),
                PackageSymbol("Create", SymbolType.FUNCTION, "func Create(name string) (*File, error)")
            )
            
            "strings" -> listOf(
                PackageSymbol("Contains", SymbolType.FUNCTION, "func Contains(s, substr string) bool"),
                PackageSymbol("HasPrefix", SymbolType.FUNCTION, "func HasPrefix(s, prefix string) bool"),
                PackageSymbol("HasSuffix", SymbolType.FUNCTION, "func HasSuffix(s, suffix string) bool"),
                PackageSymbol("Split", SymbolType.FUNCTION, "func Split(s, sep string) []string"),
                PackageSymbol("Join", SymbolType.FUNCTION, "func Join(elems []string, sep string) string"),
                PackageSymbol("Replace", SymbolType.FUNCTION, "func Replace(s, old, new string, n int) string"),
                PackageSymbol("ToLower", SymbolType.FUNCTION, "func ToLower(s string) string"),
                PackageSymbol("ToUpper", SymbolType.FUNCTION, "func ToUpper(s string) string")
            )
            
            "units" -> listOf(
                // Physics units
                PackageSymbol("Meter", SymbolType.CONSTANT, "const Meter Unit"),
                PackageSymbol("Kilogram", SymbolType.CONSTANT, "const Kilogram Unit"),
                PackageSymbol("Second", SymbolType.CONSTANT, "const Second Unit"),
                PackageSymbol("Ampere", SymbolType.CONSTANT, "const Ampere Unit"),
                PackageSymbol("Kelvin", SymbolType.CONSTANT, "const Kelvin Unit"),
                PackageSymbol("Mole", SymbolType.CONSTANT, "const Mole Unit"),
                PackageSymbol("Candela", SymbolType.CONSTANT, "const Candela Unit"),
                // Derived units
                PackageSymbol("Newton", SymbolType.CONSTANT, "const Newton Unit"),
                PackageSymbol("Joule", SymbolType.CONSTANT, "const Joule Unit"),
                PackageSymbol("Watt", SymbolType.CONSTANT, "const Watt Unit"),
                PackageSymbol("Pascal", SymbolType.CONSTANT, "const Pascal Unit"),
                PackageSymbol("Volt", SymbolType.CONSTANT, "const Volt Unit"),
                // Functions
                PackageSymbol("Convert", SymbolType.FUNCTION, "func Convert(value float64, from, to Unit) float64"),
                PackageSymbol("String", SymbolType.FUNCTION, "func String(unit Unit) string"),
                PackageSymbol("Parse", SymbolType.FUNCTION, "func Parse(s string) (Unit, error)")
            )
            
            else -> emptyList()
        }
    }
    
    private fun addSymbolCompletions(result: CompletionResultSet, symbols: List<PackageSymbol>, packageName: String) {
        for (symbol in symbols) {
            val lookupElement = when (symbol.type) {
                SymbolType.FUNCTION -> {
                    LookupElementBuilder.create(symbol.name)
                        .withTypeText("$packageName function")
                        .withTailText(extractParametersFromSignature(symbol.signature), true)
                        .withInsertHandler { context, item ->
                            val editor = context.editor
                            val caretOffset = editor.caretModel.offset
                            editor.document.insertString(caretOffset, "()")
                            editor.caretModel.moveToOffset(caretOffset + 1)
                        }
                }
                
                SymbolType.VARIABLE -> {
                    LookupElementBuilder.create(symbol.name)
                        .withTypeText("$packageName variable")
                        .withTailText(extractTypeFromSignature(symbol.signature), true)
                }
                
                SymbolType.CONSTANT -> {
                    LookupElementBuilder.create(symbol.name)
                        .withTypeText("$packageName constant")
                        .withTailText(extractTypeFromSignature(symbol.signature), true)
                }
                
                SymbolType.TYPE -> {
                    LookupElementBuilder.create(symbol.name)
                        .withTypeText("$packageName type")
                        .withTailText(extractTypeFromSignature(symbol.signature), true)
                }
                
                SymbolType.METHOD -> {
                    LookupElementBuilder.create(symbol.name)
                        .withTypeText("$packageName method")
                        .withTailText(extractParametersFromSignature(symbol.signature), true)
                        .withInsertHandler { context, item ->
                            val editor = context.editor
                            val caretOffset = editor.caretModel.offset
                            editor.document.insertString(caretOffset, "()")
                            editor.caretModel.moveToOffset(caretOffset + 1)
                        }
                }
            }
            
            result.addElement(lookupElement)
        }
    }
    
    private fun extractParametersFromSignature(signature: String): String {
        val regex = Regex("""\((.*?)\)""")
        val match = regex.find(signature)
        return if (match != null && match.groupValues[1].isNotEmpty()) {
            "(${match.groupValues[1]})"
        } else {
            "()"
        }
    }
    
    private fun extractTypeFromSignature(signature: String): String {
        // Try to extract type information from various signature formats
        return when {
            signature.contains("const ") -> signature.substringAfter("const ").substringBefore(" ")
            signature.contains("var ") -> signature.substringAfter("var ").substringBefore(" ")
            signature.contains("type ") -> signature.substringAfter("type ").substringBefore(" ")
            else -> ""
        }
    }
}