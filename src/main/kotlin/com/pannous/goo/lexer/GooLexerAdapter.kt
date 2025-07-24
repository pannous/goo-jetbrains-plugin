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
        
        val char = buffer[currentOffset]
        
        when {
            // Handle whitespace
            char.isWhitespace() -> {
                while (currentOffset < endOffset && buffer[currentOffset].isWhitespace()) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                currentToken = TokenType.WHITE_SPACE
            }
            
            // Handle comments starting with # (only when preceded by whitespace or at start of line)
            char == '#' -> {
                val prevChar = if (tokenStart > 0) buffer[tokenStart - 1] else ' '
                if (prevChar.isWhitespace() || tokenStart == 0) {
                    // It's a comment - consume until end of line
                    while (currentOffset < endOffset && buffer[currentOffset] != '\n') {
                        currentOffset++
                    }
                    tokenEnd = currentOffset
                    currentToken = GooTokenTypes.COMMENT
                } else {
                    // It's a hash operator (e.g., z#1)
                    currentOffset++
                    tokenEnd = currentOffset
                    currentToken = GooTokenTypes.OPERATOR
                }
            }
            
            // Handle / - either // comment or division operator
            char == '/' -> {
                if (currentOffset + 1 < endOffset && buffer[currentOffset + 1] == '/') {
                    // It's a // comment - consume until end of line
                    while (currentOffset < endOffset && buffer[currentOffset] != '\n') {
                        currentOffset++
                    }
                    tokenEnd = currentOffset
                    currentToken = GooTokenTypes.COMMENT
                } else {
                    // It's a division operator
                    currentOffset++
                    tokenEnd = currentOffset
                    currentToken = GooTokenTypes.OPERATOR
                }
            }
            
            // Handle string literals
            char == '"' -> {
                currentOffset++ // Skip opening quote
                while (currentOffset < endOffset && buffer[currentOffset] != '"') {
                    if (buffer[currentOffset] == '\\' && currentOffset + 1 < endOffset) {
                        currentOffset += 2 // Skip escaped character
                    } else {
                        currentOffset++
                    }
                }
                if (currentOffset < endOffset) {
                    currentOffset++ // Skip closing quote
                }
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.STRING
            }
            
            // Handle numbers
            char.isDigit() -> {
                while (currentOffset < endOffset && (buffer[currentOffset].isDigit() || buffer[currentOffset] == '.')) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.NUMBER
            }
            
            // Handle Goo-specific unicode operators
            char == '≠' -> {
                currentOffset++
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.OPERATOR
            }
            
            char == '¬' -> {
                currentOffset++
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.OPERATOR
            }
            
            char == 'ø' -> {
                currentOffset++
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.KEYWORD
            }
            
            // Handle operators and punctuation (excluding / which is handled above for // comments)
            char in "(){}[],:;=<>!&|+-*%." -> {
                // Handle multi-character operators
                if (currentOffset + 1 < endOffset) {
                    val twoChar = buffer.substring(currentOffset, currentOffset + 2)
                    when (twoChar) {
                        "==", "!=", "<=", ">=", "&&", "||", ":=" -> {
                            currentOffset += 2
                            tokenEnd = currentOffset
                            currentToken = GooTokenTypes.OPERATOR
                            return
                        }
                    }
                }
                currentOffset++
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.OPERATOR
            }
            
            // Handle identifiers and keywords
            char.isLetter() || char == '_' -> {
                while (currentOffset < endOffset && (buffer[currentOffset].isLetterOrDigit() || buffer[currentOffset] == '_')) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                val text = buffer.substring(tokenStart, tokenEnd).toString()
                
                // Check if it's a Goo keyword
                currentToken = when (text) {
                    "and", "or", "not", "def", "void", "func", "if", "else", "for", "while", 
                    "return", "break", "continue", "package", "import", "var", "const",
                    "printf", "check", "typeof" -> GooTokenTypes.KEYWORD
                    else -> GooTokenTypes.IDENTIFIER
                }
            }
            
            // Handle any other character
            else -> {
                currentOffset++
                tokenEnd = currentOffset
                currentToken = GooTokenTypes.IDENTIFIER
            }
        }
    }

    override fun getState(): Int = 0
    override fun getTokenType(): IElementType? = currentToken
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset
}