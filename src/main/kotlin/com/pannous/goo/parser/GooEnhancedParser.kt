package com.pannous.goo.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder  
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Simple parser that accepts any token sequence to avoid crashes.
 * This is the safest approach for go-to-definition without complex PSI.
 */
class GooEnhancedParser : PsiParser {
    
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        
        // Simple parsing - just consume all tokens
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        
        rootMarker.done(root)
        return builder.treeBuilt
    }
}