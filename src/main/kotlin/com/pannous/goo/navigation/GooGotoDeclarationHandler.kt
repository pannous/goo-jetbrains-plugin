package com.pannous.goo.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.pannous.goo.psi.GooFile
import com.pannous.goo.psi.GooIdentifier

/**
 * Custom Go-to-Declaration handler for Goo language constructs
 */
class GooGotoDeclarationHandler : GotoDeclarationHandler {
    
    companion object {
        // Cache for available packages to avoid repeated filesystem access
        private var packageCache: Set<String>? = null
        private var packageCacheTime: Long = 0
        private const val CACHE_DURATION_MS = 30000 // 30 seconds
        
        private fun getAvailablePackages(): Set<String> {
            val currentTime = System.currentTimeMillis()
            if (packageCache == null || (currentTime - packageCacheTime) > CACHE_DURATION_MS) {
                packageCache = discoverPackages()
                packageCacheTime = currentTime
            }
            return packageCache ?: emptySet()
        }
        
        private fun discoverPackages(): Set<String> {
            val packages = mutableSetOf<String>()
            try {
                val goSrcDir = java.io.File("/opt/other/go/src")
                if (goSrcDir.exists() && goSrcDir.isDirectory) {
                    goSrcDir.listFiles()?.forEach { dir ->
                        if (dir.isDirectory && !dir.name.startsWith(".")) {
                            // Check if the package has any .go files (excluding test files)
                            val hasGoFiles = dir.listFiles()?.any { file ->
                                file.isFile && file.name.endsWith(".go") && !file.name.endsWith("_test.go")
                            } ?: false
                            
                            if (hasGoFiles) {
                                packages.add(dir.name)
                                println("GooGotoDeclaration: Discovered package: ${dir.name}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("GooGotoDeclaration: Error discovering packages: ${e.message}")
            }
            println("GooGotoDeclaration: Total discovered packages: ${packages.size}")
            return packages
        }
    }
    
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        
        // Only handle .goo files
        val file = sourceElement?.containingFile
        if (file == null || !file.name.endsWith(".goo")) {
            return null
        }
        
        // Get the text at cursor position
        val element = sourceElement ?: return null
        val name = element.text
        
        // Debug logging
        println("GooGotoDeclaration: Processing element '$name' of type ${element.javaClass.simpleName}")
        
        // Check if this is a Goo package import - be more flexible with element types
        if (isGooPackageImport(element, name)) {
            println("GooGotoDeclaration: Found Goo package import: $name")
            return arrayOf(createVirtualGooPackage(element, name))
        } else {
            // Debug: check if we stripped quotes and try again
            val cleanName = name.removePrefix("\"").removeSuffix("\"").removePrefix("'").removeSuffix("'")
            if (cleanName != name && isGooPackageImport(element, cleanName)) {
                println("GooGotoDeclaration: Found Goo package import after cleaning quotes: $cleanName")
                return arrayOf(createVirtualGooPackage(element, cleanName))
            }
            println("GooGotoDeclaration: '$name' (cleaned: '$cleanName') not recognized as package import")
        }
        
        // Check if this is a Goo package member access
        val packageMember = getPackageMemberInfo(element, name)
        if (packageMember != null) {
            println("GooGotoDeclaration: Found package member: ${packageMember.first}.${packageMember.second}")
            return arrayOf(createVirtualPackageMember(element, packageMember.first, packageMember.second))
        }
        
        // Check if this is a local variable declaration
        val localVarDeclaration = findLocalVariableDeclaration(element, name)
        if (localVarDeclaration != null) {
            println("GooGotoDeclaration: Found local variable declaration: $name")
            return arrayOf(localVarDeclaration)
        }
        
        return null
    }
    
    private fun isGooPackageImport(element: PsiElement, name: String): Boolean {
        if (name !in getAvailablePackages()) return false
        
        // Check if this is within an import statement - be more flexible
        val fileText = element.containingFile.text
        val offset = element.textOffset
        
        // Look for import statements containing this element
        val lines = fileText.split('\n')
        for (line in lines) {
            if (line.trim().startsWith("import") && 
                (line.contains("\"$name\"") || line.contains("'$name'"))) {
                return true
            }
        }
        
        // Alternative: check if the element is directly within quotes after import
        val before = fileText.substring(0, offset + name.length)
        val after = fileText.substring(offset)
        
        if (before.contains("import") && 
            (before.endsWith("\"$name") || before.endsWith("'$name")) &&
            (after.startsWith("\"") || after.startsWith("'"))) {
            return true
        }
        
        return false
    }
    
    private fun getPackageMemberInfo(element: PsiElement, name: String): Pair<String, String>? {
        // Method 1: Check siblings
        val prevSibling = element.prevSibling
        if (prevSibling?.text == ".") {
            val packageElement = prevSibling.prevSibling
            if (packageElement != null) {
                val packageName = packageElement.text
                
                if (packageName in getAvailablePackages()) {
                    // For any discovered package, accept the member name
                    // We'll verify it exists when we open the source file
                    return Pair(packageName, name)
                }
            }
        }
        
        // Method 2: Text-based analysis for more robust detection
        val fileText = element.containingFile.text
        val offset = element.textOffset
        
        // Look backward for package.member pattern
        val beforeText = fileText.substring(0, offset)
        val regex = Regex("(\\w+)\\.$")
        val match = regex.find(beforeText.reversed())
        
        if (match != null) {
            val packageName = match.groupValues[1].reversed()
            if (packageName in getAvailablePackages()) {
                // For any discovered package, accept the member name
                // We'll verify it exists when we open the source file
                return Pair(packageName, name)
            }
        }
        
        return null
    }
    
    private fun findLocalVariableDeclaration(element: PsiElement, variableName: String): PsiElement? {
        val file = element.containingFile
        val text = file.text
        val currentOffset = element.textOffset
        
        // Common variable declaration patterns in Goo/Go
        val patterns = listOf(
            // var declarations: var name type, var name = value
            Regex("\\bvar\\s+$variableName\\s*(?:[\\w\\[\\]\\*]+\\s*)?(?:=|\\n)"),
            // Short variable declarations: name := value
            Regex("\\b$variableName\\s*:=\\s*"),
            // Function parameters: func name(param type)
            Regex("\\bfunc\\s+\\w+\\s*\\([^)]*\\b$variableName\\s+[\\w\\[\\]\\*]+"),
            // For loop variables: for name := range, for i, name := range
            Regex("\\bfor\\s+(?:\\w+\\s*,\\s*)?$variableName\\s*:?=\\s*(?:range\\s+)?")
        )
        
        val lines = text.split('\n')
        
        for ((lineIndex, line) in lines.withIndex()) {
            // Only look at lines before the current position
            val document = com.intellij.psi.PsiDocumentManager.getInstance(file.project).getDocument(file)
            if (document != null && lineIndex < document.lineCount) {
                val lineStartOffset = document.getLineStartOffset(lineIndex)
                if (lineStartOffset >= currentOffset) continue // Skip lines after current position
            }
            
            for (pattern in patterns) {
                val match = pattern.find(line)
                if (match != null) {
                    println("GooGotoDeclaration: Found variable declaration '$variableName' at line ${lineIndex + 1}: ${line.trim()}")
                    
                    try {
                        if (document != null && lineIndex < document.lineCount) {
                            val lineStartOffset = document.getLineStartOffset(lineIndex)
                            
                            // Find the exact position of the variable name in the declaration
                            val variableIndex = line.indexOf(variableName)
                            if (variableIndex >= 0) {
                                val exactOffset = lineStartOffset + variableIndex
                                val elementAtOffset = file.findElementAt(exactOffset)
                                if (elementAtOffset != null) {
                                    println("GooGotoDeclaration: Navigating to variable declaration at offset $exactOffset")
                                    return elementAtOffset
                                }
                            }
                            
                            // Fallback to line start
                            return file.findElementAt(lineStartOffset) ?: file
                        }
                    } catch (e: Exception) {
                        println("GooGotoDeclaration: Error finding variable declaration: ${e.message}")
                    }
                }
            }
        }
        
        return null
    }
    
    private fun createVirtualGooPackage(element: PsiElement, packageName: String): PsiElement {
        return findRealSourceFile(element, packageName, null) ?: createFallbackElement(element, packageName, null)
    }
    
    private fun createVirtualPackageMember(element: PsiElement, packageName: String, memberName: String): PsiElement {
        return findRealSourceFile(element, packageName, memberName) ?: createFallbackElement(element, packageName, memberName)
    }
    
    private fun findRealSourceFile(element: PsiElement, packageName: String, memberName: String?): PsiElement? {
        val project = element.project
        val virtualFileManager = com.intellij.openapi.vfs.VirtualFileManager.getInstance()
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)
        
        // Try to find the best source file for the package
        val packageDir = "/opt/other/go/src/$packageName"
        val candidateFiles = listOf(
            "$packageName.go",  // Main package file (e.g., strings.go, units.go)
            "doc.go",           // Documentation file
            "main.go"           // Main file for executable packages
        )
        
        // First, try the candidate files in order
        for (fileName in candidateFiles) {
            val sourceFilePath = "$packageDir/$fileName"
            try {
                val virtualFile = virtualFileManager.findFileByUrl("file://$sourceFilePath")
                if (virtualFile != null && virtualFile.exists()) {
                    val psiFile = psiManager.findFile(virtualFile)
                    if (psiFile != null) {
                        println("GooGotoDeclaration: Found package file $sourceFilePath")
                        
                        // If looking for a specific member, try to find it in this file
                        if (memberName != null) {
                            val memberElement = findMemberInFile(psiFile, memberName)
                            if (memberElement != null && memberElement != psiFile) {
                                return memberElement // Found the member in this file
                            }
                        } else {
                            return psiFile // Return the package file
                        }
                    }
                }
            } catch (e: Exception) {
                println("GooGotoDeclaration: Error checking file $sourceFilePath: ${e.message}")
            }
        }
        
        // If looking for a specific member and not found in main files, search all .go files
        if (memberName != null) {
            try {
                val packageDirFile = java.io.File(packageDir)
                if (packageDirFile.exists() && packageDirFile.isDirectory) {
                    val goFiles = packageDirFile.listFiles { _, name -> 
                        name.endsWith(".go") && !name.endsWith("_test.go")
                    } ?: emptyArray()
                    
                    for (goFile in goFiles) {
                        val virtualFile = virtualFileManager.findFileByUrl("file://${goFile.absolutePath}")
                        if (virtualFile != null && virtualFile.exists()) {
                            val psiFile = psiManager.findFile(virtualFile)
                            if (psiFile != null) {
                                val memberElement = findMemberInFile(psiFile, memberName)
                                if (memberElement != null && memberElement != psiFile) {
                                    println("GooGotoDeclaration: Found $memberName in ${goFile.name}")
                                    return memberElement
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("GooGotoDeclaration: Error searching for member $memberName in $packageName: ${e.message}")
            }
        }
        
        // Fallback: return the first .go file we can find in the package
        try {
            val packageDirFile = java.io.File(packageDir)
            if (packageDirFile.exists() && packageDirFile.isDirectory) {
                val firstGoFile = packageDirFile.listFiles { _, name -> 
                    name.endsWith(".go") && !name.endsWith("_test.go")
                }?.firstOrNull()
                
                if (firstGoFile != null) {
                    val virtualFile = virtualFileManager.findFileByUrl("file://${firstGoFile.absolutePath}")
                    if (virtualFile != null && virtualFile.exists()) {
                        val psiFile = psiManager.findFile(virtualFile)
                        if (psiFile != null) {
                            println("GooGotoDeclaration: Fallback to ${firstGoFile.name} for package $packageName")
                            return psiFile
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("GooGotoDeclaration: Error with fallback for $packageName: ${e.message}")
        }
        
        return null
    }
    
    private fun findMemberInFile(psiFile: com.intellij.psi.PsiFile, memberName: String): PsiElement? {
        val text = psiFile.text
        val lines = text.split('\n')
        
        // Enhanced patterns to match different Go declaration styles
        val patterns = listOf(
            // Function definitions: func MemberName(...) or MemberName = func(...)
            Regex("^\\s*func\\s+$memberName\\s*\\("),
            Regex("^\\s*$memberName\\s*=\\s*func\\s*\\("),
            
            // Variable/constant declarations with proper whitespace handling
            Regex("^\\s*(var|const)\\s+$memberName\\b"),
            Regex("^\\s*$memberName\\s*=\\s*\\w"),  // MemberName = ...
            
            // Global variable declarations in var blocks
            Regex("^\\s*$memberName\\s+[=\\w]"),  // Handle "Km  = NewUnit..." pattern
            
            // Type definitions: type MemberName
            Regex("^\\s*type\\s+$memberName\\s+"),
            
            // Method definitions: func (receiver) MemberName
            Regex("^\\s*func\\s*\\([^)]*\\)\\s*$memberName\\s*\\(")
        )
        
        for ((lineIndex, line) in lines.withIndex()) {
            for (pattern in patterns) {
                if (pattern.containsMatchIn(line) && line.contains(memberName)) {
                    println("GooGotoDeclaration: Found $memberName at line ${lineIndex + 1}: ${line.trim()}")
                    
                    // Try to find the exact PSI element at this line
                    try {
                        val document = com.intellij.psi.PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile)
                        if (document != null && lineIndex < document.lineCount) {
                            val lineStartOffset = document.getLineStartOffset(lineIndex)
                            
                            // Find the exact position of the member name in the line
                            val memberIndex = line.indexOf(memberName)
                            if (memberIndex >= 0) {
                                val exactOffset = lineStartOffset + memberIndex
                                val elementAtOffset = psiFile.findElementAt(exactOffset)
                                if (elementAtOffset != null) {
                                    println("GooGotoDeclaration: Navigating to exact offset $exactOffset for $memberName")
                                    return elementAtOffset
                                }
                            }
                            
                            // Fallback to line start if exact position not found
                            return psiFile.findElementAt(lineStartOffset) ?: psiFile
                        }
                    } catch (e: Exception) {
                        println("GooGotoDeclaration: Error finding element at line $lineIndex: ${e.message}")
                    }
                }
            }
        }
        
        println("GooGotoDeclaration: Member $memberName not found in ${psiFile.name}, returning file")
        return psiFile // Return the file if we can't find the specific member
    }
    
    private fun createFallbackElement(element: PsiElement, packageName: String, memberName: String?): PsiElement {
        // Create a virtual documentation element as fallback
        return object : PsiElement by element {
            override fun toString(): String = if (memberName != null) {
                "$packageName.$memberName"
            } else {
                "Goo Package: $packageName"
            }
            
            override fun getText(): String {
                return if (memberName != null) {
                    "// $packageName.$memberName (source file not found at /opt/other/go/src/$packageName/$packageName.go)"
                } else {
                    "// Package $packageName (source file not found at /opt/other/go/src/$packageName/$packageName.go)"
                }
            }
        }
    }
}