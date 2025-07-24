package com.pannous.goo

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object GooFileType : LanguageFileType(GooLanguage) {
    override fun getName(): String = "Goo"
    override fun getDescription(): String = "Goo language files"
    override fun getDefaultExtension(): String = "goo"
    override fun getIcon(): Icon? = GooIcons.FILE
}