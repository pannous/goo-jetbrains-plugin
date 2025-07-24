package com.pannous.goo.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import javax.swing.JLabel

class GooRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    var scriptName = ""

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<GooRunConfiguration>() {
            override fun resetEditorFrom(s: GooRunConfiguration) {}
            override fun applyEditorTo(s: GooRunConfiguration) {}
            override fun createEditor(): JComponent = JLabel("Goo Run Configuration")
        }
    }

    override fun checkConfiguration() {
        // Basic validation
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : RunProfileState {
            override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): com.intellij.execution.ExecutionResult {
                val commandLine = GeneralCommandLine()
                commandLine.exePath = "go"  // Simple test
                commandLine.addParameter("version")
                
                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                return com.intellij.execution.DefaultExecutionResult(null, processHandler)
            }
        }
    }
}