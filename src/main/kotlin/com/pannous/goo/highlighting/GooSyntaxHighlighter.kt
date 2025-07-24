package com.pannous.goo.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.pannous.goo.lexer.GooLexer
import com.pannous.goo.lexer.GooTokenTypes

class GooSyntaxHighlighter : SyntaxHighlighterBase() {
    
    companion object {
        val KEYWORD = TextAttributesKey.createTextAttributesKey("GOO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING = TextAttributesKey.createTextAttributesKey("GOO_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER = TextAttributesKey.createTextAttributesKey("GOO_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val COMMENT = TextAttributesKey.createTextAttributesKey("GOO_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val IDENTIFIER = TextAttributesKey.createTextAttributesKey("GOO_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    }
    
    override fun getHighlightingLexer(): Lexer = GooLexer()
    
    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            GooTokenTypes.KEYWORD -> arrayOf(KEYWORD)
            GooTokenTypes.STRING -> arrayOf(STRING)
            GooTokenTypes.NUMBER -> arrayOf(NUMBER)
            GooTokenTypes.COMMENT -> arrayOf(COMMENT)
            GooTokenTypes.IDENTIFIER -> arrayOf(IDENTIFIER)
            else -> emptyArray()
        }
    }
}