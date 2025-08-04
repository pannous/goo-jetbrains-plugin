package com.pannous.goo.psi

import com.intellij.psi.tree.IElementType
import com.pannous.goo.GooLanguage

class GooElementType(debugName: String) : IElementType(debugName, GooLanguage)

object GooElementTypes {
    @JvmField val FUNCTION_DECLARATION = GooElementType("FUNCTION_DECLARATION")
    @JvmField val VARIABLE_DECLARATION = GooElementType("VARIABLE_DECLARATION")
    @JvmField val ASSIGNMENT = GooElementType("ASSIGNMENT")
    @JvmField val BLOCK = GooElementType("BLOCK")
    @JvmField val COMMENT = GooElementType("COMMENT")
    @JvmField val EXPRESSION_STATEMENT = GooElementType("EXPRESSION_STATEMENT")
    @JvmField val IDENTIFIER = GooElementType("IDENTIFIER")
}