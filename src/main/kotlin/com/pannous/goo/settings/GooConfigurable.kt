package com.pannous.goo.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class GooConfigurable(private val project: Project) : Configurable {
    
    private val settings = GooSettings.getInstance(project)
    
    private lateinit var compilerPathField: TextFieldWithBrowseButton
    private lateinit var goRootField: TextFieldWithBrowseButton  
    private lateinit var enableIntegrationCheckBox: JBCheckBox
    private lateinit var panel: JPanel
    
    override fun getDisplayName(): String = "Goo Compiler"
    
    override fun createComponent(): JComponent {
        compilerPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Goo Compiler", 
                "Select the Goo compiler executable (usually 'go' in the bin directory)",
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }
        
        goRootField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select GOROOT", 
                "Select the Goo installation directory (contains bin, src, pkg folders)",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
            )
        }
        
        enableIntegrationCheckBox = JBCheckBox("Enable compiler integration for real-time error detection")
        
        panel = FormBuilder.createFormBuilder()
            .addComponent(enableIntegrationCheckBox)
            .addLabeledComponent(
                JBLabel("Compiler path:"), 
                compilerPathField, 
                1, 
                false
            )
            .addLabeledComponent(
                JBLabel("GOROOT:"), 
                goRootField, 
                1, 
                false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        reset()
        return panel
    }
    
    override fun isModified(): Boolean {
        return compilerPathField.text != settings.getCompilerPath() ||
               goRootField.text != settings.getGoRoot() ||
               enableIntegrationCheckBox.isSelected != settings.isCompilerIntegrationEnabled()
    }
    
    override fun apply() {
        settings.setCompilerPath(compilerPathField.text)
        settings.setGoRoot(goRootField.text)
        settings.setCompilerIntegrationEnabled(enableIntegrationCheckBox.isSelected)
    }
    
    override fun reset() {
        compilerPathField.text = settings.getCompilerPath()
        goRootField.text = settings.getGoRoot()
        enableIntegrationCheckBox.isSelected = settings.isCompilerIntegrationEnabled()
    }
}