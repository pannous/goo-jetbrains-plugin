package com.pannous.goo.lexer

import com.intellij.psi.tree.IElementType
import com.pannous.goo.GooLanguage

class GooTokenType(debugName: String) : IElementType(debugName, GooLanguage) {
    override fun toString(): String = "GooTokenType.${super.toString()}"
}