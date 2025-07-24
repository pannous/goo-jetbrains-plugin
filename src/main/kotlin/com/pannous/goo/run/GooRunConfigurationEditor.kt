package com.pannous.goo.run

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

    init {
        // Simple setup without deprecated methods
        filePathField.addActionListener {
            // File chooser logic can be added here if needed
        }
        
        workingDirectoryField.addActionListener {
            // Directory chooser logic can be added here if needed  
        }

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JLabel("Goo file:"), filePathField)
            .addLabeledComponent(JLabel("Working directory:"), workingDirectoryField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun resetEditorFrom(configuration: GooRunConfiguration) {
        filePathField.text = configuration.filePath
        workingDirectoryField.text = configuration.workingDirectory
    }

    override fun applyEditorTo(configuration: GooRunConfiguration) {
        configuration.filePath = filePathField.text
        configuration.workingDirectory = workingDirectoryField.text
    }

    override fun createEditor(): JComponent = panel
}