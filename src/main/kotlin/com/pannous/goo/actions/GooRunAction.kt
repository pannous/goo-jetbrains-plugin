package com.pannous.goo.actions

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.pannous.goo.run.GooRunConfiguration
import com.pannous.goo.run.GooRunConfigurationType

class GooRunAction : AnAction("Run Goo File") {
    
    companion object {
        private val LOG = Logger.getInstance(GooRunAction::class.java)
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("GooRunAction: actionPerformed called")
        
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        LOG.info("GooRunAction: project=$project, file=${file?.path}")
        
        if (project == null || file == null) {
            LOG.warn("GooRunAction: Missing project or file")
            return
        }
        
        if (file.extension != "goo") {
            LOG.info("GooRunAction: File is not a .goo file: ${file.extension}")
            return
        }
        
        try {
            val runManager = RunManager.getInstance(project)
            val factory = GooRunConfigurationType().configurationFactories[0]
            val settings = runManager.createConfiguration("Run ${file.name}", factory)
            val configuration = settings.configuration as GooRunConfiguration
            
            LOG.info("GooRunAction: Created configuration, setting filePath to: ${file.path}")
            
            configuration.filePath = file.path
            configuration.scriptName = file.path // Backup
            configuration.workingDirectory = file.parent.path
            
            LOG.info("GooRunAction: Configuration setup complete - filePath='${configuration.filePath}', workingDirectory='${configuration.workingDirectory}'")
            
            runManager.addConfiguration(settings)
            runManager.selectedConfiguration = settings
            
            val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID)
            if (executor != null) {
                LOG.info("GooRunAction: Executing configuration with executor: ${executor.id}")
                ProgramRunnerUtil.executeConfiguration(settings, executor)
            } else {
                LOG.error("GooRunAction: Could not find DefaultRunExecutor")
            }
        } catch (e: Exception) {
            LOG.error("GooRunAction: Error creating or executing run configuration", e)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isGooFile = file?.extension == "goo"
        e.presentation.isEnabledAndVisible = isGooFile
        LOG.debug("GooRunAction: update called - file=${file?.path}, isGooFile=$isGooFile")
    }
}