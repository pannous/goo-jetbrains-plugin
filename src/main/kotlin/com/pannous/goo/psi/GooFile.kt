package com.pannous.goo.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.pannous.goo.GooFileType
import com.pannous.goo.GooLanguage

class GooFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GooLanguage) {
    
    override fun getFileType() = GooFileType
    
    override fun toString(): String = "Goo File"
}