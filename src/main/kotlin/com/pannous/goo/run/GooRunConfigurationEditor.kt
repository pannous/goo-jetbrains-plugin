package com.pannous.goo.run

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class GooRunConfigurationEditor : SettingsEditor<GooRunConfiguration>() {
    private val panel: JPanel
    private val filePathField = TextFieldWithBrowseButton()
    private val workingDirectoryField = TextFieldWithBrowseButton()
    private val environmentVariablesComponent = EnvironmentVariablesComponent()

    init {
        filePathField.addBrowseFolderListener(
            "Select Goo File",
            "Choose a .goo file to run",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor("goo")
        )
        
        workingDirectoryField.addBrowseFolderListener(
            "Select Working Directory", 
            "Choose working directory",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JLabel("Goo file:"), filePathField)
            .addLabeledComponent(JLabel("Working directory:"), workingDirectoryField)
            .addLabeledComponent(JLabel("Environment variables:"), environmentVariablesComponent)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun resetEditorFrom(configuration: GooRunConfiguration) {
        filePathField.text = configuration.filePath
        workingDirectoryField.text = configuration.workingDirectory
        environmentVariablesComponent.envs = configuration.environmentVariables
    }

    override fun applyEditorTo(configuration: GooRunConfiguration) {
        configuration.filePath = filePathField.text
        configuration.workingDirectory = workingDirectoryField.text
        configuration.environmentVariables.clear()
        configuration.environmentVariables.putAll(environmentVariablesComponent.envs)
    }

    override fun createEditor(): JComponent = panel
}