package com.pannous.goo.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class GooRunConfigurationProducer : LazyRunConfigurationProducer<GooRunConfiguration>() {
    
    companion object {
        private val LOG = Logger.getInstance(GooRunConfigurationProducer::class.java)
    }

    override fun getConfigurationFactory(): ConfigurationFactory {
        LOG.info("GooRunConfigurationProducer: getConfigurationFactory called")
        return GooRunConfigurationType().configurationFactories[0]
    }

    override fun setupConfigurationFromContext(
        configuration: GooRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        LOG.info("GooRunConfigurationProducer: setupConfigurationFromContext called")
        
        val virtualFile = context.location?.virtualFile
        LOG.info("GooRunConfigurationProducer: Virtual file: ${virtualFile?.path}")
        
        if (virtualFile != null && virtualFile.extension == "goo") {
            val filePath = virtualFile.path
            LOG.info("GooRunConfigurationProducer: Setting up configuration for .goo file: $filePath")
            
            configuration.filePath = filePath
            configuration.scriptName = filePath // Backup
            configuration.workingDirectory = virtualFile.parent.path
            configuration.name = "Run ${virtualFile.name}"
            
            LOG.info("GooRunConfigurationProducer: Configuration set up with filePath='${configuration.filePath}', workingDirectory='${configuration.workingDirectory}', name='${configuration.name}'")
            return true
        }
        
        LOG.info("GooRunConfigurationProducer: File is not a .goo file or is null, skipping")
        return false
    }

    override fun isConfigurationFromContext(
        configuration: GooRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        LOG.info("GooRunConfigurationProducer: isConfigurationFromContext called")
        
        val virtualFile = context.location?.virtualFile
        if (virtualFile?.extension == "goo") {
            val match = configuration.filePath == virtualFile.path
            LOG.info("GooRunConfigurationProducer: Checking if configuration matches context: filePath='${configuration.filePath}', virtualFile='${virtualFile.path}', match=$match")
            return match
        }
        
        LOG.info("GooRunConfigurationProducer: Context is not a .goo file")
        return false
    }
}