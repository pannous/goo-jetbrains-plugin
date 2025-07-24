package com.pannous.goo.lexer

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerPosition
import com.intellij.psi.tree.IElementType

class GooLexer : Lexer() {
    private val lexerAdapter = GooLexerAdapter()
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        lexerAdapter.start(buffer, startOffset, endOffset, initialState)
    }

    override fun advance() = lexerAdapter.advance()
    override fun getState() = lexerAdapter.state
    override fun getTokenType() = lexerAdapter.tokenType
    override fun getTokenStart() = lexerAdapter.tokenStart
    override fun getTokenEnd() = lexerAdapter.tokenEnd
    override fun getBufferSequence() = lexerAdapter.bufferSequence
    override fun getBufferEnd() = lexerAdapter.bufferEnd
    override fun getCurrentPosition(): LexerPosition = lexerAdapter.currentPosition
    override fun restore(position: LexerPosition) = lexerAdapter.restore(position)
}