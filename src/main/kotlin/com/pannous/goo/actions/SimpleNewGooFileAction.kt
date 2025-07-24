package com.pannous.goo.actions

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import java.util.*

class SimpleNewGooFileAction : AnAction("Goo File", "Create new Goo file", com.pannous.goo.GooIcons.FILE) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = e.getData(CommonDataKeys.PSI_ELEMENT)?.containingFile?.containingDirectory
            ?: return
        
        val fileName = Messages.showInputDialog(
            project,
            "Enter file name:",
            "New Goo File",
            com.pannous.goo.GooIcons.FILE,
            "main",
            null
        ) ?: return
        
        val fullFileName = if (fileName.endsWith(".goo")) fileName else "$fileName.goo"
        
        WriteAction.run<RuntimeException> {
            try {
                val templateManager = FileTemplateManager.getDefaultInstance()
                val template = templateManager.getInternalTemplate("Goo File.goo")
                
                val properties = Properties()
                properties.setProperty("NAME", fileName)
                
                val psiFile = FileTemplateUtil.createFromTemplate(
                    template,
                    fullFileName,
                    properties,
                    directory as PsiDirectory
                )
                
                // Open the new file in editor
                if (psiFile is com.intellij.psi.PsiFile) {
                    val virtualFile = psiFile.virtualFile
                    if (virtualFile != null) {
                        FileEditorManager.getInstance(project).openFile(virtualFile, true)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Could not create file: ${ex.message}",
                    "Error Creating Goo File"
                )
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}