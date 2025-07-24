package com.pannous.goo.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class GooParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        
        // Simple parser that accepts any token sequence
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        
        rootMarker.done(root)
        return builder.treeBuilt
    }
}