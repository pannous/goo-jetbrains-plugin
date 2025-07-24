package com.pannous.goo.actions

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.pannous.goo.run.GooRunConfiguration
import com.pannous.goo.run.GooRunConfigurationType

class GooRunAction : AnAction("Run Goo File") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        if (file.extension != "goo") return
        
        val runManager = RunManager.getInstance(project)
        val factory = GooRunConfigurationType().configurationFactories[0]
        val settings = runManager.createConfiguration("Run ${file.name}", factory)
        val configuration = settings.configuration as GooRunConfiguration
        
        configuration.filePath = file.path
        configuration.workingDirectory = file.parent.path
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings
        
        val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID)
        if (executor != null) {
            ProgramRunnerUtil.executeConfiguration(settings, executor)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.extension == "goo"
    }
}