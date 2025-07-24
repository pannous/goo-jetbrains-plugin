package com.pannous.goo.formatting

import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes

class GooSpacingBuilder(settings: CodeStyleSettings) : SpacingBuilder(settings, GooLanguage) {
    
    init {
        // Minimal spacing rules - don't apply to ALL operators/keywords
        // Only specific cases to avoid excessive spacing
        
        // Brace formatting only
        before(GooTokenTypes.LBRACE).spaces(1)
        after(GooTokenTypes.LBRACE).lineBreakInCode()
        before(GooTokenTypes.RBRACE).lineBreakInCode()
        
        // Very minimal - let the code keep its existing spacing mostly
    }
}