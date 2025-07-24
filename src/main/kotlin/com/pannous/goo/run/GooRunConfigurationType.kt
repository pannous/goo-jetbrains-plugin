package com.pannous.goo.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.pannous.goo.GooIcons
import javax.swing.Icon

class GooRunConfigurationType : ConfigurationType {
    
    companion object {
        private val LOG = Logger.getInstance(GooRunConfigurationType::class.java)
        
        init {
            LOG.warn("=== GooRunConfigurationType CLASS LOADED ===")
        }
    }
    
    override fun getDisplayName(): String {
        LOG.info("GooRunConfigurationType: getDisplayName() called")
        return "Goo"
    }
    
    override fun getConfigurationTypeDescription(): String {
        LOG.info("GooRunConfigurationType: getConfigurationTypeDescription() called")
        return "Goo run configuration"
    }
    
    override fun getIcon(): Icon {
        LOG.info("GooRunConfigurationType: getIcon() called")
        return GooIcons.FILE
    }
    
    override fun getId(): String {
        LOG.info("GooRunConfigurationType: getId() called")
        return "GooRunConfiguration"
    }
    
    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        LOG.warn("=== GooRunConfigurationType: getConfigurationFactories() called ===")
        return arrayOf(GooConfigurationFactory(this))
    }
}

class GooConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    
    companion object {
        private val LOG = Logger.getInstance(GooConfigurationFactory::class.java)
        
        init {
            LOG.warn("=== GooConfigurationFactory CLASS LOADED ===")
        }
    }
    
    override fun getId(): String {
        LOG.info("GooConfigurationFactory: getId() called")
        return "Goo"
    }
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        LOG.warn("=== GooConfigurationFactory: createTemplateConfiguration() called for project: ${project.name} ===")
        val config = GooRunConfiguration(project, this, "Goo")
        LOG.info("GooConfigurationFactory: Created configuration: $config")
        return config
    }
}