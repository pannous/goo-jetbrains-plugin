package com.pannous.goo

import com.intellij.lang.Language

object GooLanguage : Language("Goo") {
    override fun getDisplayName(): String = "Goo"
    override fun isCaseSensitive(): Boolean = true
}