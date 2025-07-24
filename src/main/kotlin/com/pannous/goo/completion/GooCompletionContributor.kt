package com.pannous.goo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.pannous.goo.GooLanguage

class GooCompletionContributor : CompletionContributor() {
    
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(GooLanguage),
            GooKeywordCompletionProvider()
        )
    }
}

class GooKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    private val gooKeywords = listOf(
        "and", "or", "not", "ø", "printf", "check", "typeof", "def", "void"
    )
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        gooKeywords.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .withTypeText("Goo keyword")
                    .withBoldness(true)
            )
        }
    }
}