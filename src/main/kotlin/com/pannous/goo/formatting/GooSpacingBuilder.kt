package com.pannous.goo.formatting

import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes

class GooSpacingBuilder(settings: CodeStyleSettings) : SpacingBuilder(settings, GooLanguage) {
    
    init {
        // NO SPACING RULES AT ALL - just preserve existing spacing
        // This will stop the formatter from adding excessive spaces
        // and only focus on indentation
    }
}