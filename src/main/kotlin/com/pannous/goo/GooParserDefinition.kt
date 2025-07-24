package com.pannous.goo

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.pannous.goo.lexer.GooLexer
import com.pannous.goo.parser.GooParser
import com.pannous.goo.psi.GooFile

class GooParserDefinition : ParserDefinition {
    
    companion object {
        val FILE = IFileElementType(GooLanguage)
    }
    
    override fun createLexer(project: Project?): Lexer = GooLexer()
    
    override fun createParser(project: Project?): PsiParser = GooParser()
    
    override fun getFileNodeType(): IFileElementType = FILE
    
    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY
    
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
    
    override fun createElement(node: ASTNode?): PsiElement {
        throw AssertionError("createElement should never be called for PsiFile nodes")
    }
    
    override fun createFile(viewProvider: FileViewProvider): PsiFile = GooFile(viewProvider)
}