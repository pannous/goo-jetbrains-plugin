package com.pannous.goo.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class GooRunConfigurationOptions : RunConfigurationOptions() {
    private val myFilePath: StoredProperty<String?> = string("").provideDelegate(this, "filePath")
    private val myWorkingDirectory: StoredProperty<String?> = string("").provideDelegate(this, "workingDirectory")
    
    var filePath: String?
        get() = myFilePath.getValue(this)
        set(value) = myFilePath.setValue(this, value)
    
    var workingDirectory: String?
        get() = myWorkingDirectory.getValue(this)
        set(value) = myWorkingDirectory.setValue(this, value)
}