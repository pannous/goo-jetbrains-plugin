package com.pannous.goo.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler

class GooRefactoringSupportProvider : RefactoringSupportProvider() {
    
    override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean = true
    
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean = true
    
    override fun getIntroduceVariableHandler(): RefactoringActionHandler = GooIntroduceVariableHandler()
    
    override fun isAvailable(context: PsiElement): Boolean = true
}