package com.pannous.goo.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import com.pannous.goo.lexer.GooTokenTypes
import com.pannous.goo.psi.GooFile

class GooRenameProcessor : RenamePsiElementProcessor() {
    
    override fun canProcessElement(element: PsiElement): Boolean {
        // Can process identifier tokens in Goo files
        return element.node?.elementType == GooTokenTypes.IDENTIFIER && 
               element.containingFile is GooFile
    }
    
    override fun isInplaceRenameSupported(): Boolean = true
    
    override fun substituteElementToRename(element: PsiElement, editor: Editor?): PsiElement? {
        return element
    }
    
    override fun findReferences(
        element: PsiElement,
        searchScope: SearchScope,
        searchInCommentsAndStrings: Boolean
    ): Collection<com.intellij.psi.PsiReference> {
        // Find all references to this element within the search scope
        val references = mutableListOf<com.intellij.psi.PsiReference>()
        
        // Use the built-in reference search
        val referencesSearch = com.intellij.psi.search.searches.ReferencesSearch.search(element, searchScope)
        references.addAll(referencesSearch.findAll())
        
        return references
    }
    
    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<UsageInfo>,
        listener: RefactoringElementListener?
    ) {
        // Perform the actual rename operation
        super.renameElement(element, newName, usages, listener)
        
        // Notify that rename is complete
        listener?.elementRenamed(element)
    }
    
    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        // Add the main element to be renamed
        allRenames[element] = newName
        
        // Could add related elements here if needed
        // For example, if renaming a function, might also rename related elements
    }
    
    override fun getElementToSearchInStringsAndComments(element: PsiElement): PsiElement? {
        return element
    }
    
    override fun getHelpID(element: PsiElement): String? {
        return "refactoring.rename"
    }
}