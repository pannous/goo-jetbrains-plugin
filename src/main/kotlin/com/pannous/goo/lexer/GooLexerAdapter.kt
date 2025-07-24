package com.pannous.goo.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class GooLexerAdapter : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0  
    private var currentOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var currentToken: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentOffset = startOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        advance()
    }

    override fun advance() {
        if (currentOffset >= endOffset) {
            currentToken = null
            return
        }

        tokenStart = currentOffset
        
        // Skip whitespace
        while (currentOffset < endOffset && buffer[currentOffset].isWhitespace()) {
            currentOffset++
        }
        
        if (currentOffset >= endOffset) {
            currentToken = null
            return
        }
        
        tokenStart = currentOffset
        
        // Simple tokenization - just consume non-whitespace characters
        while (currentOffset < endOffset && !buffer[currentOffset].isWhitespace()) {
            currentOffset++
        }
        
        tokenEnd = currentOffset
        currentToken = GooTokenTypes.IDENTIFIER
    }

    override fun getState(): Int = 0
    override fun getTokenType(): IElementType? = currentToken
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset
}