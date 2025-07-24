package com.pannous.goo.run

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner

class GooRunner : DefaultProgramRunner() {
    override fun getRunnerId(): String = "GooRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return DefaultRunExecutor.EXECUTOR_ID == executorId && profile is GooRunConfiguration
    }
}