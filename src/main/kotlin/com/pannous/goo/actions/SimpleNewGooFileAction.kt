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
        
        // Get the target directory - try multiple ways to find it
        val directory = e.getData(CommonDataKeys.PSI_ELEMENT)?.let { element ->
            when {
                element is PsiDirectory -> element
                element.containingFile?.containingDirectory != null -> element.containingFile?.containingDirectory
                else -> null
            }
        } ?: e.getData(CommonDataKeys.PSI_FILE)?.containingDirectory
        ?: e.getData(CommonDataKeys.VIRTUAL_FILE)?.let { virtualFile ->
            if (virtualFile.isDirectory) {
                com.intellij.psi.PsiManager.getInstance(project).findDirectory(virtualFile)
            } else {
                com.intellij.psi.PsiManager.getInstance(project).findDirectory(virtualFile.parent)
            }
        } ?: return
        
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
                
                // Try to get the template - try multiple names
                val template = try {
                    templateManager.getInternalTemplate("Goo File.goo")
                } catch (e: Exception) {
                    try {
                        templateManager.getTemplate("Goo File.goo")
                    } catch (e2: Exception) {
                        // Create a simple template if none exists
                        null
                    }
                }
                
                val psiFile = if (template != null) {
                    val properties = Properties()
                    properties.setProperty("NAME", fileName.removeSuffix(".goo"))
                    
                    FileTemplateUtil.createFromTemplate(
                        template,
                        fullFileName,
                        properties,
                        directory
                    )
                } else {
                    // Fallback: create file directly with basic content
                    val newFile = directory.createFile(fullFileName)
                    val document = com.intellij.psi.PsiDocumentManager.getInstance(project).getDocument(newFile)
                    document?.setText("#!/usr/bin/env goo\n\nprintf(\"Hello, World!\")\n")
                    newFile
                }
                
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
        val project = e.project
        val enabled = project != null && (
            e.getData(CommonDataKeys.PSI_ELEMENT) != null ||
            e.getData(CommonDataKeys.PSI_FILE) != null ||
            e.getData(CommonDataKeys.VIRTUAL_FILE) != null
        )
        e.presentation.isEnabledAndVisible = enabled
    }
}