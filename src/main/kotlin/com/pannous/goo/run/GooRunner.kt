package com.pannous.goo.run

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner

class GooRunner : ProgramRunner<RunnerSettings> {
    override fun getRunnerId(): String = "GooRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return DefaultRunExecutor.EXECUTOR_ID == executorId && profile is GooRunConfiguration
    }
    
    override fun execute(environment: ExecutionEnvironment) {
        val state = environment.state ?: return
        state.execute(environment.executor, this)
    }
}