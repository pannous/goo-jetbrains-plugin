package com.pannous.goo.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.pannous.goo.GooIcons
import javax.swing.Icon

class GooRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Goo"
    override fun getConfigurationTypeDescription(): String = "Goo run configuration"
    override fun getIcon(): Icon = GooIcons.FILE
    override fun getId(): String = "GooRunConfiguration"
    
    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(GooConfigurationFactory(this))
    }
}

class GooConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "Goo"
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return GooRunConfiguration(project, this, "Goo")
    }
}