package com.pannous.goo.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameHandler
import com.intellij.refactoring.rename.RenameDialog
import com.pannous.goo.psi.GooFile
import com.pannous.goo.lexer.GooTokenTypes

class GooRenameHandler : RenameHandler {
    
    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val file = dataContext.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE)
        return file is GooFile
    }
    
    override fun isRenaming(dataContext: DataContext): Boolean {
        return isAvailableOnDataContext(dataContext)
    }
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
        if (editor == null || file == null) return
        
        val element = getElementToRename(editor, file) ?: return
        
        // Create and show rename dialog
        val dialog = RenameDialog(project, element, null, editor)
        dialog.show()
    }
    
    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext) {
        if (elements.isEmpty()) return
        
        val element = elements[0]
        val dialog = RenameDialog(project, element, null, null)
        dialog.show()
    }
    
    private fun getElementToRename(editor: Editor, file: PsiFile): PsiElement? {
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset)
        
        return when {
            // Find identifier elements that can be renamed
            element?.node?.elementType == GooTokenTypes.IDENTIFIER -> element
            // If we're on whitespace, try to find adjacent identifier
            element?.text?.trim()?.isEmpty() == true -> {
                // Look for identifier before or after current position
                file.findElementAt(offset - 1)?.takeIf { 
                    it.node?.elementType == GooTokenTypes.IDENTIFIER 
                } ?: file.findElementAt(offset + 1)?.takeIf {
                    it.node?.elementType == GooTokenTypes.IDENTIFIER
                }
            }
            else -> null
        }
    }
}