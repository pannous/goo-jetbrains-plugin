package com.pannous.goo.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.tree.IElementType
import com.pannous.goo.compiler.GooCompiler
import com.pannous.goo.lexer.GooTokenTypes

/**
 * Enhanced parser that leverages the Goo compiler for better syntax understanding.
 * Falls back to simple parsing if compiler integration fails.
 */
class GooEnhancedParser : PsiParser {
    
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        
        // Try to use compiler-assisted parsing in background
        if (shouldUseCompilerAssistedParsing(builder)) {
            parseWithCompilerAssistance(builder)
        } else {
            // Fallback to basic parsing
            parseBasic(builder)
        }
        
        rootMarker.done(root)
        return builder.treeBuilt
    }
    
    /**
     * Determine if we should attempt compiler-assisted parsing
     */
    private fun shouldUseCompilerAssistedParsing(builder: PsiBuilder): Boolean {
        // Only use compiler assistance in read-action context and not during indexing
        return !ApplicationManager.getApplication().isUnitTestMode &&
               !ApplicationManager.getApplication().isHeadlessEnvironment
    }
    
    /**
     * Parse using compiler feedback for better structure understanding
     */
    private fun parseWithCompilerAssistance(builder: PsiBuilder) {
        // For now, implement basic parsing with token-level improvements
        // In the future, this could use compiler AST output for structure
        parseWithTokenRecognition(builder)
    }
    
    /**
     * Enhanced parsing that recognizes Goo-specific constructs
     */
    private fun parseWithTokenRecognition(builder: PsiBuilder) {
        while (!builder.eof()) {
            when (builder.tokenType) {
                GooTokenTypes.KEYWORD -> {
                    val keywordText = builder.tokenText
                    when (keywordText) {
                        "def", "func" -> parseFunctionDefinition(builder)
                        "class" -> parseClassDefinition(builder)
                        "enum" -> parseEnumDefinition(builder)
                        "try" -> parseTryBlock(builder)
                        "if" -> parseIfStatement(builder)
                        else -> builder.advanceLexer()
                    }
                }
                GooTokenTypes.OPERATOR -> {
                    val operatorText = builder.tokenText
                    when (operatorText) {
                        "=>" -> parseLambdaExpression(builder)
                        else -> builder.advanceLexer()
                    }
                }
                else -> builder.advanceLexer()
            }
        }
    }
    
    /**
     * Basic fallback parsing - accepts any token sequence
     */
    private fun parseBasic(builder: PsiBuilder) {
        while (!builder.eof()) {
            builder.advanceLexer()
        }
    }
    
    /**
     * Parse function definition (def/func keyword)
     */
    private fun parseFunctionDefinition(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'def' or 'func'
        
        // Skip to next meaningful token or end of statement
        while (!builder.eof() && builder.tokenText != "{" && builder.tokenText != "\n") {
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.KEYWORD) // Mark as function definition
    }
    
    /**
     * Parse class definition
     */
    private fun parseClassDefinition(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'class'
        
        // Skip to opening brace or end of line
        while (!builder.eof() && builder.tokenText != "{" && builder.tokenText != "\n") {
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.KEYWORD) // Mark as class definition
    }
    
    /**
     * Parse enum definition
     */
    private fun parseEnumDefinition(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'enum'
        
        // Skip to opening brace or end of line
        while (!builder.eof() && builder.tokenText != "{" && builder.tokenText != "\n") {
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.KEYWORD) // Mark as enum definition
    }
    
    /**
     * Parse try-catch block
     */
    private fun parseTryBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'try'
        
        // Skip try block content
        var braceDepth = 0
        while (!builder.eof()) {
            when (builder.tokenText) {
                "{" -> braceDepth++
                "}" -> {
                    braceDepth--
                    if (braceDepth <= 0) {
                        builder.advanceLexer()
                        break
                    }
                }
                "catch" -> {
                    if (braceDepth == 0) break
                }
            }
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.KEYWORD) // Mark as try block
    }
    
    /**
     * Parse if statement (with potential truthy/falsey support)
     */
    private fun parseIfStatement(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'if'
        
        // Skip to opening brace or end of line
        while (!builder.eof() && builder.tokenText != "{" && builder.tokenText != "\n") {
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.KEYWORD) // Mark as if statement
    }
    
    /**
     * Parse lambda expression (=>)
     */
    private fun parseLambdaExpression(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume '=>'
        
        // Skip lambda body
        while (!builder.eof() && builder.tokenText != "\n" && builder.tokenText != "," && builder.tokenText != ")") {
            builder.advanceLexer()
        }
        
        marker.done(GooTokenTypes.OPERATOR) // Mark as lambda
    }
}