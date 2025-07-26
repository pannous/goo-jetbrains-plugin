package com.pannous.goo.debugger

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase
import com.pannous.goo.GooFileType

class GooLineBreakpointType : XLineBreakpointTypeBase("goo", "Goo Line Breakpoints", GooDebuggerEditorsProvider()) {
    
    override fun canPutAt(file: VirtualFile, line: Int, project: Project): Boolean {
        return file.fileType == GooFileType
    }
    
    override fun createBreakpointProperties(file: VirtualFile, line: Int): XBreakpointProperties<*>? {
        return null
    }
}

class GooDebuggerEditorsProvider : XDebuggerEditorsProviderBase() {
    override fun getFileType() = GooFileType
    
    override fun createExpressionCodeFragment(project: Project, text: String, context: PsiElement?, isPhysical: Boolean): PsiFile? {
        return null
    }
}