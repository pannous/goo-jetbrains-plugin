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

    override fun getOptions(): RunConfigurationOptions {
        return super.getOptions()
    }

    var filePath: String
        get() = options.filePath ?: ""
        set(value) {
            options.filePath = value
        }

    var workingDirectory: String
        get() = options.workingDirectory ?: ""
        set(value) {
            options.workingDirectory = value
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

class GooRunConfigurationOptions : BaseState() {
    var filePath: String? by string()
    var workingDirectory: String? by string()
}