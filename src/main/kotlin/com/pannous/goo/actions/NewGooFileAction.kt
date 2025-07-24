package com.pannous.goo.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.pannous.goo.GooIcons

class NewGooFileAction : CreateFileFromTemplateAction("Goo File", "Create new Goo file", GooIcons.FILE) {
    
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Goo File")
            .addKind("Goo file", GooIcons.FILE, "GooFile")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Goo File $newName"
    }
}