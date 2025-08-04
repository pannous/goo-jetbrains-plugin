package com.pannous.goo.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.pannous.goo.psi.GooFile
import com.pannous.goo.lexer.GooTokenTypes
import java.io.File

/**
 * Enhanced go-to-declaration handler that can navigate to:
 * 1. Local declarations within the same .goo file
 * 2. Go library functions using safe file-based lookup
 */
class GooGotoDeclarationHandler : GotoDeclarationHandler {
    
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        
        if (sourceElement == null || sourceElement.containingFile !is GooFile) {
            return null
        }
        
        // Only handle identifiers
        if (sourceElement.node?.elementType != GooTokenTypes.IDENTIFIER) {
            return null
        }
        
        val identifierText = sourceElement.text
        if (identifierText.isBlank()) {
            return null
        }
        
        val file = sourceElement.containingFile
        val project = file.project
        val targets = mutableListOf<PsiElement>()
        
        // Check if this is a qualified call (e.g., sdl.GetMouseState)
        val isQualifiedCall = checkIfQualifiedCall(sourceElement, file.text, offset)
        
        if (isQualifiedCall.first) {
            // Handle package.Function calls
            val packageName = isQualifiedCall.second
            val functionName = identifierText
            
            // Try to find Go library declarations
            findGoLibraryDeclaration(project, packageName, functionName)?.let { targets.add(it) }
        } else {
            // Handle local declarations within the same file
            findLocalDeclarations(file, identifierText, targets)
        }
        
        return if (targets.isNotEmpty()) targets.toTypedArray() else null
    }
    
    private fun checkIfQualifiedCall(element: PsiElement, fileText: String, offset: Int): Pair<Boolean, String> {
        // Look backwards from the element position to see if there's a package prefix
        val elementStart = element.textRange.startOffset
        val textBefore = fileText.substring(0, elementStart)
        
        // Look for pattern: packageName.functionName
        val qualifiedRegex = Regex("(\\w+)\\.\\s*$")
        val match = qualifiedRegex.find(textBefore)
        
        return if (match != null) {
            Pair(true, match.groupValues[1])
        } else {
            Pair(false, "")
        }
    }
    
    private fun findGoLibraryDeclaration(project: Project, packageName: String, functionName: String): PsiElement? {
        try {
            // Strategy 1: Try to find in project's Go files first (safest)
            findInProjectGoFiles(project, packageName, functionName)?.let { return it }
            
            // Strategy 2: Look in common Go library locations
            findInCommonGoLibraries(project, packageName, functionName)?.let { return it }
            
            // Strategy 3: Try Go module cache if available
            findInGoModuleCache(project, packageName, functionName)?.let { return it }
            
        } catch (e: Exception) {
            // Fail silently to avoid crashes - this is the key to safety
            return null
        }
        
        return null
    }
    
    private fun findInProjectGoFiles(project: Project, packageName: String, functionName: String): PsiElement? {
        try {
            // Look in the current project's Go files first
            val goFiles = FilenameIndex.getAllFilesByExt(project, "go", GlobalSearchScope.projectScope(project))
            
            for (goFile in goFiles) {
                val psiFile = PsiManager.getInstance(project).findFile(goFile)
                psiFile?.let { file ->
                    val fileText = file.text
                    
                    // Check if this file belongs to the right package
                    if (fileText.contains("package $packageName")) {
                        val funcPattern = Regex("func\\s+$functionName\\s*\\(")
                        val match = funcPattern.find(fileText)
                        if (match != null) {
                            val functionOffset = match.range.first + 5 // Skip "func "
                            return file.findElementAt(functionOffset)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fail silently
        }
        return null
    }
    
    private fun findInCommonGoLibraries(project: Project, packageName: String, functionName: String): PsiElement? {
        try {
            val commonGoPackages = mapOf(
                "sdl" to listOf("github.com/veandco/go-sdl2/sdl", "sdl"),
                "fmt" to listOf("fmt"),
                "os" to listOf("os"),
                "io" to listOf("io"),
                "net" to listOf("net"),
                "http" to listOf("net/http"),
                "json" to listOf("encoding/json"),
                "time" to listOf("time"),
                "strings" to listOf("strings"),
                "strconv" to listOf("strconv"),
                "context" to listOf("context"),
                "sync" to listOf("sync"),
                "log" to listOf("log")
            )
            
            val possiblePaths = commonGoPackages[packageName] ?: return null
            
            // Search in all Go files
            val goFiles = FilenameIndex.getAllFilesByExt(project, "go", GlobalSearchScope.allScope(project))
            
            for (path in possiblePaths) {
                for (goFile in goFiles) {
                    if (goFile.path.contains(path.replace("/", File.separator)) || 
                        goFile.parent?.name == packageName) {
                        
                        val psiFile = PsiManager.getInstance(project).findFile(goFile)
                        psiFile?.let { file ->
                            val fileText = file.text
                            
                            // Look for function declaration
                            val funcPattern = Regex("func\\s+$functionName\\s*\\(")
                            val match = funcPattern.find(fileText)
                            if (match != null) {
                                val functionOffset = match.range.first + 5
                                return file.findElementAt(functionOffset)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fail silently
        }
        return null
    }
    
    private fun findInGoModuleCache(project: Project, packageName: String, functionName: String): PsiElement? {
        try {
            // Look in Go module cache locations (GOPATH/pkg/mod)
            val userHome = System.getProperty("user.home")
            val goPaths = listOf(
                "$userHome/go/pkg/mod",
                System.getenv("GOPATH")?.let { "$it/pkg/mod" }
            ).filterNotNull()
            
            for (goPath in goPaths) {
                val goFiles = FilenameIndex.getAllFilesByExt(project, "go", GlobalSearchScope.allScope(project))
                
                for (goFile in goFiles) {
                    if (goFile.path.contains(goPath) && goFile.path.contains(packageName)) {
                        val psiFile = PsiManager.getInstance(project).findFile(goFile)
                        psiFile?.let { file ->
                            val fileText = file.text
                            val funcPattern = Regex("func\\s+$functionName\\s*\\(")
                            val match = funcPattern.find(fileText)
                            if (match != null) {
                                val functionOffset = match.range.first + 5
                                return file.findElementAt(functionOffset)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fail silently
        }
        return null
    }
    
    private fun findLocalDeclarations(file: PsiElement, identifierText: String, targets: MutableList<PsiElement>) {
        // Simple text-based search for function declarations within the same file
        val fileText = file.text
        val lines = fileText.lines()
        
        for ((lineIndex, line) in lines.withIndex()) {
            // Look for function declarations: "func functionName" or "def functionName"
            val funcRegex = Regex("\\b(func|def)\\s+($identifierText)\\b")
            val funcMatch = funcRegex.find(line)
            if (funcMatch != null) {
                // Find the PSI element at this position
                val lineStartOffset = lines.take(lineIndex).sumOf { it.length + 1 }
                val functionNameOffset = lineStartOffset + funcMatch.range.first + funcMatch.groupValues[1].length + 1
                val target = file.findElementAt(functionNameOffset)
                if (target != null && target.text == identifierText) {
                    targets.add(target)
                }
            }
            
            // Look for variable declarations: "variableName :="
            val varRegex = Regex("\\b($identifierText)\\s*:=")
            val varMatch = varRegex.find(line)
            if (varMatch != null) {
                val lineStartOffset = lines.take(lineIndex).sumOf { it.length + 1 }
                val variableNameOffset = lineStartOffset + varMatch.range.first
                val target = file.findElementAt(variableNameOffset)
                if (target != null && target.text == identifierText) {
                    targets.add(target)
                }
            }
        }
    }
}