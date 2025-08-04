package com.pannous.goo.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.pannous.goo.lexer.GooTokenTypes
import com.pannous.goo.psi.GooElementTypes

class GooEnhancedParser : PsiParser {
    
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        
        parseFile(builder)
        
        rootMarker.done(root)
        return builder.treeBuilt
    }
    
    private fun parseFile(builder: PsiBuilder) {
        while (!builder.eof()) {
            when {
                // Skip whitespace and comments
                builder.tokenType?.toString() == "WHITE_SPACE" -> builder.advanceLexer()
                builder.tokenType == GooTokenTypes.COMMENT -> parseComment(builder)
                
                // Parse statements
                isAtFunction(builder) -> parseFunction(builder)
                isAtVariableDeclaration(builder) -> parseVariableDeclaration(builder)
                isAtAssignment(builder) -> parseAssignment(builder)
                
                // Parse identifiers and other statements
                builder.tokenType == GooTokenTypes.IDENTIFIER -> parseIdentifierOrStatement(builder)
                
                // Parse other statements
                else -> parseStatement(builder)
            }
        }
    }
    
    private fun parseComment(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(GooElementTypes.COMMENT)
    }
    
    private fun isAtFunction(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text == "func" || text == "def"
    }
    
    private fun parseFunction(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // func/def keyword
        builder.advanceLexer()
        
        // function name identifier
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            val identifierMarker = builder.mark()
            builder.advanceLexer()
            identifierMarker.done(GooElementTypes.IDENTIFIER)
        }
        
        // parameters (everything until {)
        while (!builder.eof() && builder.tokenText != "{") {
            builder.advanceLexer()
        }
        
        // body (everything until matching })
        if (builder.tokenText == "{") {
            parseBlock(builder)
        }
        
        marker.done(GooElementTypes.FUNCTION_DECLARATION)
    }
    
    private fun isAtVariableDeclaration(builder: PsiBuilder): Boolean {
        if (builder.tokenType != GooTokenTypes.IDENTIFIER) return false
        
        // Look ahead for ":=" pattern
        val mark = builder.mark()
        var tokensAhead = 0
        var foundAssignment = false
        
        // Skip identifier
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            tokensAhead++
        }
        
        // Look for := within reasonable distance
        while (!builder.eof() && tokensAhead < 5) {
            if (builder.tokenText == ":=") {
                foundAssignment = true
                break
            }
            if (builder.tokenText?.contains("\n") == true) break
            builder.advanceLexer()
            tokensAhead++
        }
        
        mark.rollbackTo()
        return foundAssignment
    }
    
    private fun parseVariableDeclaration(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // variable name identifier
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            val identifierMarker = builder.mark()
            builder.advanceLexer()
            identifierMarker.done(GooElementTypes.IDENTIFIER)
        }
        
        // skip whitespace
        while (builder.tokenText?.isBlank() == true) {
            builder.advanceLexer()
        }
        
        // := operator
        if (builder.tokenText == ":=") {
            builder.advanceLexer()
        }
        
        // value expression (until newline or specific terminators)
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text?.contains("\n") == true || text == ";" || text == "}") {
                break
            }
            builder.advanceLexer()
        }
        
        marker.done(GooElementTypes.VARIABLE_DECLARATION)
    }
    
    private fun isAtAssignment(builder: PsiBuilder): Boolean {
        if (builder.tokenType != GooTokenTypes.IDENTIFIER) return false
        
        // Look ahead for "=" pattern (not ":=" which is variable declaration)
        val mark = builder.mark()
        var tokensAhead = 0
        var foundAssignment = false
        
        // Skip identifier
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            tokensAhead++
        }
        
        // Look for = within reasonable distance, but not :=
        while (!builder.eof() && tokensAhead < 5) {
            if (builder.tokenText == "=") {
                foundAssignment = true
                break
            }
            if (builder.tokenText == ":=" || builder.tokenText?.contains("\n") == true) break
            builder.advanceLexer()
            tokensAhead++
        }
        
        mark.rollbackTo()
        return foundAssignment
    }
    
    private fun parseAssignment(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // variable name identifier
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            val identifierMarker = builder.mark()
            builder.advanceLexer()
            identifierMarker.done(GooElementTypes.IDENTIFIER)
        }
        
        // skip whitespace
        while (builder.tokenText?.isBlank() == true) {
            builder.advanceLexer()
        }
        
        // = operator
        if (builder.tokenText == "=") {
            builder.advanceLexer()
        }
        
        // value expression
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text?.contains("\n") == true || text == ";" || text == "}") {
                break
            }
            builder.advanceLexer()
        }
        
        marker.done(GooElementTypes.ASSIGNMENT)
    }
    
    private fun parseBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // opening brace
        if (builder.tokenText == "{") {
            builder.advanceLexer()
        }
        
        // parse block contents
        var braceCount = 1
        while (!builder.eof() && braceCount > 0) {
            when (builder.tokenText) {
                "{" -> {
                    braceCount++
                    builder.advanceLexer()
                }
                "}" -> {
                    braceCount--
                    builder.advanceLexer()
                }
                else -> {
                    if (isAtFunction(builder)) {
                        parseFunction(builder)
                    } else if (isAtVariableDeclaration(builder)) {
                        parseVariableDeclaration(builder)
                    } else {
                        builder.advanceLexer()
                    }
                }
            }
        }
        
        marker.done(GooElementTypes.BLOCK)
    }
    
    private fun parseStatement(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // Parse until end of statement
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text?.contains("\n") == true || text == ";" || text == "}") {
                break
            }
            builder.advanceLexer()
        }
        
        marker.done(GooElementTypes.EXPRESSION_STATEMENT)
    }
    
    private fun parseIdentifierOrStatement(builder: PsiBuilder) {
        // This handles standalone identifiers (like function calls)
        val marker = builder.mark()
        
        // Mark the identifier itself
        if (builder.tokenType == GooTokenTypes.IDENTIFIER) {
            val identifierMarker = builder.mark()
            builder.advanceLexer()
            identifierMarker.done(GooElementTypes.IDENTIFIER)
        }
        
        // Parse the rest of the statement
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text?.contains("\n") == true || text == ";" || text == "}") {
                break
            }
            builder.advanceLexer()
        }
        
        marker.done(GooElementTypes.EXPRESSION_STATEMENT)
    }
}