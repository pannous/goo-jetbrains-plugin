package com.pannous.goo.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.pannous.goo.GooLanguage
import com.pannous.goo.lexer.GooTokenTypes
// GooFormattingModelBuilder.kt
class GooFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(ctx: FormattingContext): FormattingModel {
    val root = ctx.psiElement.containingFile.node
    val block = GooBlock(
      root,
      Wrap.createWrap(WrapType.NONE, false),
      Alignment.createAlignment(),
      GooSpacingBuilder(ctx.codeStyleSettings)
    )
    return FormattingModelProvider.createFormattingModelForPsiFile(ctx.containingFile, block, ctx.codeStyleSettings)
  }
}