package com.pannous.goo.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class GooRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<GooRunConfigurationOptions>(project, factory, name) {

    private val gooOptions = GooRunConfigurationOptions()
    
    override fun getOptions(): RunConfigurationOptions {
        return gooOptions
    }

    var filePath: String
        get() = gooOptions.filePath ?: ""
        set(value) {
            gooOptions.filePath = value
        }

    var workingDirectory: String
        get() = gooOptions.workingDirectory ?: ""
        set(value) {
            gooOptions.workingDirectory = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return GooRunConfigurationEditor()
    }

    override fun checkConfiguration() {
        if (filePath.isEmpty()) {
            throw RuntimeConfigurationError("File path is not specified")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return GooRunProfileState(environment, this)
    }
}

class GooRunConfigurationOptions : RunConfigurationOptions() {
    var filePath: String? by string()
    var workingDirectory: String? by string()
}