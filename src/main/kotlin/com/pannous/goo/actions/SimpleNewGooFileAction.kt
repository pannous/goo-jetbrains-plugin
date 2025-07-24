package com.pannous.goo.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFileFactory
import com.pannous.goo.GooFileType
import com.pannous.goo.GooLanguage

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
        
        val template = """#!/usr/bin/env goo

"""
        
        WriteAction.run<RuntimeException> {
            val psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(fullFileName, GooFileType, template)
            
            val newFile = directory.add(psiFile)
            
            // Open the new file in editor
            if (newFile is com.intellij.psi.PsiFile) {
                val virtualFile = newFile.virtualFile
                if (virtualFile != null) {
                    FileEditorManager.getInstance(project).openFile(virtualFile, true)
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}