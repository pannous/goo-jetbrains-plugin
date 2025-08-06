package com.pannous.goo.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler
import com.pannous.goo.lexer.GooTokenTypes

class GooRefactoringSupportProvider : RefactoringSupportProvider() {
    
    override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Allow in-place rename for identifiers, function names, variable names, etc.
        return when (element.node?.elementType) {
            GooTokenTypes.IDENTIFIER -> true
            else -> false
        }
    }
    
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Allow member rename for identifiers in member context
        return isInplaceRenameAvailable(element, context)
    }
    
    override fun getIntroduceVariableHandler(): RefactoringActionHandler = GooIntroduceVariableHandler()
    
    override fun isAvailable(context: PsiElement): Boolean {
        // Refactoring is available for Goo files
        return context.containingFile?.name?.endsWith(".goo") == true
    }
    
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        // Enable safe delete for identifiers
        return element.node?.elementType == GooTokenTypes.IDENTIFIER
    }
}