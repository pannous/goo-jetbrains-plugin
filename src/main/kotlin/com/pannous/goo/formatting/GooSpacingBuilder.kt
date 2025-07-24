package com.pannous.goo.formatting

import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes

class GooSpacingBuilder(settings: CodeStyleSettings) : SpacingBuilder(settings, GooLanguage) {
    
    init {
        // Basic spacing rules for Goo language
        around(GooTokenTypes.OPERATOR).spaces(1)
        after(GooTokenTypes.KEYWORD).spaces(1)
        
        // Brace spacing
        before(GooTokenTypes.LBRACE).spaces(1)
        after(GooTokenTypes.LBRACE).lineBreakInCode()
        before(GooTokenTypes.RBRACE).lineBreakInCode()
    }
}