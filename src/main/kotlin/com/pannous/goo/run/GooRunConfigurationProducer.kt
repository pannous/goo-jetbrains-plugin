package com.pannous.goo.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.pannous.goo.psi.GooFile

class GooRunConfigurationProducer : LazyRunConfigurationProducer<GooRunConfiguration>() {
    
    override fun getConfigurationFactory(): ConfigurationFactory {
        return GooRunConfigurationType().configurationFactories[0]
    }

    override fun setupConfigurationFromContext(
        configuration: GooRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val psiFile = location.psiElement?.containingFile
        
        if (psiFile !is GooFile || psiFile.virtualFile?.extension != "goo") {
            return false
        }

        configuration.name = psiFile.name
        configuration.filePath = psiFile.virtualFile.path
        configuration.workingDirectory = psiFile.virtualFile.parent.path
        
        return true
    }

    override fun isConfigurationFromContext(
        configuration: GooRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val psiFile = location.psiElement?.containingFile
        
        return psiFile is GooFile && 
               psiFile.virtualFile?.path == configuration.filePath
    }
}