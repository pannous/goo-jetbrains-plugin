package com.pannous.goo.actions

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.pannous.goo.GooCommenter
import com.pannous.goo.psi.GooFile

class GooToggleCommentAction : AnAction() {
    
    private val commenter = GooCommenter()
    
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = file is GooFile && editor != null
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        if (file !is GooFile) return
        
        WriteCommandAction.runWriteCommandAction(project, "Toggle Goo Comments", null, {
            toggleComments(editor, project)
        })
    }
    
    private fun toggleComments(editor: Editor, project: Project) {
        val document = editor.document
        val selectionModel = editor.selectionModel
        val caretModel = editor.caretModel
        
        val startLine: Int
        val endLine: Int
        
        if (selectionModel.hasSelection()) {
            // Handle selection
            startLine = document.getLineNumber(selectionModel.selectionStart)
            endLine = document.getLineNumber(selectionModel.selectionEnd)
        } else {
            // Handle current line
            startLine = document.getLineNumber(caretModel.offset)
            endLine = startLine
        }
        
        // Check if all selected lines are commented
        var allCommented = true
        for (line in startLine..endLine) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
            
            if (lineText.trim().isNotEmpty() && !commenter.isLineCommented(lineText)) {
                allCommented = false
                break
            }
        }
        
        // Toggle comments based on current state
        for (line in startLine..endLine) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
            
            val newLineText = if (allCommented) {
                // Uncomment - remove both # and // styles
                commenter.uncommentLine(lineText)
            } else {
                // Comment - add preferred # style, but only if line has content
                if (lineText.trim().isNotEmpty()) {
                    commenter.commentLine(lineText)
                } else {
                    lineText
                }
            }
            
            if (newLineText != lineText) {
                document.replaceString(lineStart, lineEnd, newLineText)
            }
        }
    }
}