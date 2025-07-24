package com.pannous.goo.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes

class GooFormattingModelBuilder : FormattingModelBuilder {
    
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val element = formattingContext.psiElement
        val settings = formattingContext.codeStyleSettings
        val containingFile = formattingContext.containingFile
        
        return FormattingModelProvider.createFormattingModelForPsiFile(
            containingFile,
            GooBlock(
                element.node,
                Wrap.createWrap(WrapType.NONE, false),
                Alignment.createAlignment(),
                createSpaceBuilder(settings)
            ),
            settings
        )
    }
    
    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
        // Minimal spacing builder - focus on indentation instead
        return SpacingBuilder(settings, GooLanguage)
    }
}