package com.pannous.goo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

class GooBuiltinMethodsProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val editor = parameters.editor ?: return
        
        // Get the text before the dot to determine the type
        val textBeforeDot = getTextBeforeDot(editor, position.textOffset)
        
        when {
            // String literals: "hello".method()
            isStringLiteral(textBeforeDot) -> addStringMethods(result)
            
            // String variables: str.method() (detected by heuristics)
            isStringVariable(textBeforeDot) -> addStringMethods(result)
            
            // Array literals: [1,2,3].method()
            isArrayLiteral(textBeforeDot) -> addArrayMethods(result)
            
            // Array variables: arr.method()
            isArrayVariable(textBeforeDot) -> addArrayMethods(result)
            
            // Number literals: 42.method()
            isNumberLiteral(textBeforeDot) -> addNumberMethods(result)
            
            // Map literals: {key: value}.method()
            isMapLiteral(textBeforeDot) -> addMapMethods(result)
            
            // Generic object methods for any context
            else -> addGenericMethods(result)
        }
    }
    
    private fun getTextBeforeDot(editor: Editor, offset: Int): String {
        return try {
            val document = editor.document
            val line = document.getLineNumber(offset)
            val lineStart = document.getLineStartOffset(line)
            val text = document.getText().substring(lineStart, offset)
            
            // Find the last complete expression before the dot
            var level = 0
            var i = text.length - 2 // Skip the dot itself
            
            while (i >= 0) {
                when (text[i]) {
                    ')' -> level++
                    '(' -> level--
                    ']' -> level++
                    '[' -> level--
                    '}' -> level++
                    '{' -> level--
                    ' ', '\t' -> if (level == 0) break
                    '=', '+', '-', '*', '/', '%', '&', '|', '^', '<', '>', '!', '?' -> if (level == 0) break
                }
                i--
            }
            
            text.substring(i + 1).trim().removeSuffix(".")
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun isStringLiteral(text: String): Boolean {
        return text.matches(Regex("^\"[^\"]*\"$")) || 
               text.matches(Regex("^'[^']*'$")) ||
               text.matches(Regex("^`[^`]*`$"))
    }
    
    private fun isStringVariable(text: String): Boolean {
        // Heuristic: variables ending with common string suffixes or containing string operations
        return text.matches(Regex("\\b\\w*(str|text|name|msg|message|content|data)\\b", RegexOption.IGNORE_CASE)) ||
               text.contains("string") ||
               text.contains("String(")
    }
    
    private fun isArrayLiteral(text: String): Boolean {
        return text.matches(Regex("^\\[.*]$"))
    }
    
    private fun isArrayVariable(text: String): Boolean {
        return text.matches(Regex("\\b\\w*(arr|array|list|slice|items|elements)\\b", RegexOption.IGNORE_CASE)) ||
               text.contains("[]") ||
               text.contains("make(")
    }
    
    private fun isNumberLiteral(text: String): Boolean {
        return text.matches(Regex("^-?\\d+(\\.\\d+)?$"))
    }
    
    private fun isMapLiteral(text: String): Boolean {
        return text.matches(Regex("^\\{.*}$")) && text.contains(":")
    }
    
    private fun addStringMethods(result: CompletionResultSet) {
        val stringMethods = listOf(
            // Enhanced Goo string methods
            MethodInfo("reverse", "string", "Reverse the string", "\"hello\".reverse() → \"olleh\""),
            MethodInfo("contains", "bool", "Check if string contains substring", "\"hello\".contains(\"ell\") → true"),
            MethodInfo("startsWith", "bool", "Check if string starts with prefix", "\"hello\".startsWith(\"he\") → true"),
            MethodInfo("endsWith", "bool", "Check if string ends with suffix", "\"hello\".endsWith(\"lo\") → true"),
            MethodInfo("toUpper", "string", "Convert to uppercase", "\"hello\".toUpper() → \"HELLO\""),
            MethodInfo("toLower", "string", "Convert to lowercase", "\"HELLO\".toLower() → \"hello\""),
            MethodInfo("trim", "string", "Remove leading and trailing whitespace", "\" hello \".trim() → \"hello\""),
            MethodInfo("trimLeft", "string", "Remove leading whitespace", "\" hello\".trimLeft() → \"hello\""),
            MethodInfo("trimRight", "string", "Remove trailing whitespace", "\"hello \".trimRight() → \"hello\""),
            MethodInfo("split", "[]string", "Split string by separator", "\"a,b,c\".split(\",\") → [\"a\", \"b\", \"c\"]"),
            MethodInfo("replace", "string", "Replace occurrences", "\"hello\".replace(\"l\", \"x\") → \"hexxo\""),
            MethodInfo("replaceAll", "string", "Replace all occurrences", "\"hello\".replaceAll(\"l\", \"x\") → \"hexxo\""),
            MethodInfo("substring", "string", "Extract substring", "\"hello\".substring(1, 4) → \"ell\""),
            MethodInfo("charAt", "rune", "Get character at index", "\"hello\".charAt(1) → 'e'"),
            MethodInfo("indexOf", "int", "Find first index of substring", "\"hello\".indexOf(\"l\") → 2"),
            MethodInfo("lastIndexOf", "int", "Find last index of substring", "\"hello\".lastIndexOf(\"l\") → 3"),
            MethodInfo("length", "int", "Get string length", "\"hello\".length() → 5"),
            MethodInfo("isEmpty", "bool", "Check if string is empty", "\"\".isEmpty() → true"),
            MethodInfo("isBlank", "bool", "Check if string is empty or whitespace", "\" \".isBlank() → true"),
            MethodInfo("repeat", "string", "Repeat string n times", "\"ha\".repeat(3) → \"hahaha\""),
            MethodInfo("padLeft", "string", "Pad string on the left", "\"5\".padLeft(3, '0') → \"005\""),
            MethodInfo("padRight", "string", "Pad string on the right", "\"5\".padRight(3, '0') → \"500\""),
            MethodInfo("lines", "[]string", "Split into lines", "\"a\\nb\\nc\".lines() → [\"a\", \"b\", \"c\"]"),
            MethodInfo("words", "[]string", "Split into words", "\"hello world\".words() → [\"hello\", \"world\"]"),
            MethodInfo("capitalize", "string", "Capitalize first letter", "\"hello\".capitalize() → \"Hello\""),
            MethodInfo("uncapitalize", "string", "Lowercase first letter", "\"Hello\".uncapitalize() → \"hello\""),
            MethodInfo("swapCase", "string", "Swap upper/lower case", "\"Hello\".swapCase() → \"hELLO\"")
        )
        
        stringMethods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withTypeText(method.returnType)
                    .withTailText("()")
                    .withInsertHandler(createInsertHandler(method))
                    .withPresentableText("${method.name}()")
                    .bold()
            )
        }
    }
    
    private fun addArrayMethods(result: CompletionResultSet) {
        val arrayMethods = listOf(
            MethodInfo("length", "int", "Get array length", "[1,2,3].length() → 3"),
            MethodInfo("size", "int", "Get array size", "[1,2,3].size() → 3"),
            MethodInfo("isEmpty", "bool", "Check if array is empty", "[].isEmpty() → true"),
            MethodInfo("first", "T", "Get first element", "[1,2,3].first() → 1"),
            MethodInfo("last", "T", "Get last element", "[1,2,3].last() → 3"),
            MethodInfo("reverse", "[]T", "Reverse the array", "[1,2,3].reverse() → [3,2,1]"),
            MethodInfo("sort", "[]T", "Sort the array", "[3,1,2].sort() → [1,2,3]"),
            MethodInfo("contains", "bool", "Check if array contains element", "[1,2,3].contains(2) → true"),
            MethodInfo("indexOf", "int", "Find index of element", "[1,2,3].indexOf(2) → 1"),
            MethodInfo("lastIndexOf", "int", "Find last index of element", "[1,2,2,3].lastIndexOf(2) → 2"),
            MethodInfo("filter", "[]T", "Filter elements", "[1,2,3,4].filter(x => x % 2 == 0) → [2,4]"),
            MethodInfo("map", "[]U", "Transform elements", "[1,2,3].map(x => x * 2) → [2,4,6]"),
            MethodInfo("reduce", "U", "Reduce to single value", "[1,2,3].reduce((a,b) => a + b) → 6"),
            MethodInfo("forEach", "void", "Execute function for each element", "[1,2,3].forEach(x => print(x))"),
            MethodInfo("find", "T", "Find first matching element", "[1,2,3].find(x => x > 1) → 2"),
            MethodInfo("findIndex", "int", "Find index of first match", "[1,2,3].findIndex(x => x > 1) → 1"),
            MethodInfo("every", "bool", "Test if all elements match", "[2,4,6].every(x => x % 2 == 0) → true"),
            MethodInfo("some", "bool", "Test if any element matches", "[1,2,3].some(x => x % 2 == 0) → true"),
            MethodInfo("join", "string", "Join elements into string", "[1,2,3].join(\",\") → \"1,2,3\""),
            MethodInfo("slice", "[]T", "Extract slice", "[1,2,3,4,5].slice(1, 3) → [2,3]"),
            MethodInfo("append", "[]T", "Append element", "[1,2].append(3) → [1,2,3]"),
            MethodInfo("prepend", "[]T", "Prepend element", "[2,3].prepend(1) → [1,2,3]"),
            MethodInfo("remove", "[]T", "Remove element at index", "[1,2,3].remove(1) → [1,3]"),
            MethodInfo("removeAll", "[]T", "Remove all occurrences", "[1,2,1,3].removeAll(1) → [2,3]"),
            MethodInfo("unique", "[]T", "Remove duplicates", "[1,2,2,3,1].unique() → [1,2,3]"),
            MethodInfo("flatten", "[]T", "Flatten nested arrays", "[[1,2],[3,4]].flatten() → [1,2,3,4]"),
            MethodInfo("zip", "[]Pair", "Zip with another array", "[1,2].zip([3,4]) → [(1,3),(2,4)]"),
            MethodInfo("sum", "number", "Sum numeric elements", "[1,2,3].sum() → 6"),
            MethodInfo("average", "number", "Average of numeric elements", "[1,2,3].average() → 2.0"),
            MethodInfo("min", "T", "Minimum element", "[3,1,2].min() → 1"),
            MethodInfo("max", "T", "Maximum element", "[3,1,2].max() → 3")
        )
        
        arrayMethods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withTypeText(method.returnType)
                    .withTailText("()")
                    .withInsertHandler(createInsertHandler(method))
                    .withPresentableText("${method.name}()")
                    .bold()
            )
        }
    }
    
    private fun addNumberMethods(result: CompletionResultSet) {
        val numberMethods = listOf(
            MethodInfo("abs", "number", "Absolute value", "(-5).abs() → 5"),
            MethodInfo("round", "int", "Round to nearest integer", "(3.7).round() → 4"),
            MethodInfo("floor", "int", "Round down", "(3.7).floor() → 3"),
            MethodInfo("ceil", "int", "Round up", "(3.2).ceil() → 4"),
            MethodInfo("sqrt", "float", "Square root", "(16).sqrt() → 4.0"),
            MethodInfo("pow", "number", "Power", "(2).pow(3) → 8"),
            MethodInfo("sin", "float", "Sine", "(0).sin() → 0.0"),
            MethodInfo("cos", "float", "Cosine", "(0).cos() → 1.0"),
            MethodInfo("tan", "float", "Tangent", "(0).tan() → 0.0"),
            MethodInfo("log", "float", "Natural logarithm", "(2.718).log() ≈ 1.0"),
            MethodInfo("log10", "float", "Base-10 logarithm", "(100).log10() → 2.0"),
            MethodInfo("toString", "string", "Convert to string", "(42).toString() → \"42\""),
            MethodInfo("toFloat", "float", "Convert to float", "(42).toFloat() → 42.0"),
            MethodInfo("toInt", "int", "Convert to integer", "(42.7).toInt() → 42"),
            MethodInfo("isEven", "bool", "Check if even", "(4).isEven() → true"),
            MethodInfo("isOdd", "bool", "Check if odd", "(3).isOdd() → true"),
            MethodInfo("isPrime", "bool", "Check if prime", "(7).isPrime() → true"),
            MethodInfo("times", "void", "Execute n times", "(3).times(i => print(i))"),
            MethodInfo("upto", "[]int", "Range up to n", "(5).upto(10) → [5,6,7,8,9,10]"),
            MethodInfo("downto", "[]int", "Range down to n", "(10).downto(5) → [10,9,8,7,6,5]")
        )
        
        numberMethods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withTypeText(method.returnType)
                    .withTailText("()")
                    .withInsertHandler(createInsertHandler(method))
                    .withPresentableText("${method.name}()")
                    .bold()
            )
        }
    }
    
    private fun addMapMethods(result: CompletionResultSet) {
        val mapMethods = listOf(
            MethodInfo("size", "int", "Get map size", "{a:1, b:2}.size() → 2"),
            MethodInfo("isEmpty", "bool", "Check if map is empty", "{}.isEmpty() → true"),
            MethodInfo("hasKey", "bool", "Check if key exists", "{a:1}.hasKey(\"a\") → true"),
            MethodInfo("hasValue", "bool", "Check if value exists", "{a:1}.hasValue(1) → true"),
            MethodInfo("keys", "[]K", "Get all keys", "{a:1, b:2}.keys() → [\"a\", \"b\"]"),
            MethodInfo("values", "[]V", "Get all values", "{a:1, b:2}.values() → [1, 2]"),
            MethodInfo("entries", "[]Pair", "Get key-value pairs", "{a:1}.entries() → [(\"a\", 1)]"),
            MethodInfo("merge", "map", "Merge with another map", "{a:1}.merge({b:2}) → {a:1, b:2}"),
            MethodInfo("filter", "map", "Filter entries", "{a:1, b:2}.filter((k,v) => v > 1) → {b:2}"),
            MethodInfo("map", "map", "Transform values", "{a:1}.map((k,v) => v*2) → {a:2}"),
            MethodInfo("forEach", "void", "Execute for each entry", "{a:1}.forEach((k,v) => print(k, v))")
        )
        
        mapMethods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withTypeText(method.returnType)
                    .withTailText("()")
                    .withInsertHandler(createInsertHandler(method))
                    .withPresentableText("${method.name}()")
                    .bold()
            )
        }
    }
    
    private fun addGenericMethods(result: CompletionResultSet) {
        val genericMethods = listOf(
            MethodInfo("toString", "string", "Convert to string", "value.toString()"),
            MethodInfo("typeof", "string", "Get type name", "value.typeof() → \"string\""),
            MethodInfo("isNull", "bool", "Check if null/nil", "value.isNull() → false"),
            MethodInfo("isNotNull", "bool", "Check if not null", "value.isNotNull() → true"),
            MethodInfo("apply", "T", "Apply function to value", "value.apply(func)"),
            MethodInfo("let", "U", "Transform value", "value.let(func)"),
            MethodInfo("also", "T", "Execute and return value", "value.also(func)")
        )
        
        genericMethods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withTypeText(method.returnType)
                    .withTailText("()")
                    .withInsertHandler(createInsertHandler(method))
                    .withPresentableText("${method.name}()")
                    .bold()
            )
        }
    }
    
    private fun createInsertHandler(method: MethodInfo): InsertHandler<com.intellij.codeInsight.lookup.LookupElement> {
        return InsertHandler { context, _ ->
            val editor = context.editor
            val caretOffset = editor.caretModel.offset
            
            // Insert parentheses and position cursor inside
            editor.document.insertString(caretOffset, "()")
            editor.caretModel.moveToOffset(caretOffset + 1)
        }
    }
    
    private data class MethodInfo(
        val name: String,
        val returnType: String,
        val description: String,
        val example: String
    )
}