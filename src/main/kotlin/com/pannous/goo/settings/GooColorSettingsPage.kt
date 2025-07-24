package com.pannous.goo.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.pannous.goo.GooIcons
import com.pannous.goo.highlighting.GooSyntaxHighlighter
import javax.swing.Icon

class GooColorSettingsPage : ColorSettingsPage {
    
    private val attributes = arrayOf(
        AttributesDescriptor("Keyword", GooSyntaxHighlighter.KEYWORD),
        AttributesDescriptor("String", GooSyntaxHighlighter.STRING),
        AttributesDescriptor("Number", GooSyntaxHighlighter.NUMBER),
        AttributesDescriptor("Comment", GooSyntaxHighlighter.COMMENT),
        AttributesDescriptor("Identifier", GooSyntaxHighlighter.IDENTIFIER),
        AttributesDescriptor("Operator", GooSyntaxHighlighter.OPERATOR)
    )
    
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = attributes
    
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    
    override fun getDisplayName(): String = "Goo"
    
    override fun getIcon(): Icon = GooIcons.FILE
    
    override fun getHighlighter(): SyntaxHighlighter = GooSyntaxHighlighter()
    
    override fun getDemoText(): String = """
        #!/usr/bin/env goo
        
        # This is a Goo comment
        printf("Hello, Goo World!")
        
        x := 42
        y := "test string"
        ptr := ø
        
        # Test Goo-specific operators
        if x and y ≠ "" {
            printf("x is truthy and y is not empty")
        }
        
        if ptr == ø {
            printf("ptr is nil")
        }
        
        # Test unicode operators
        if x ≠ 0 and ¬(x < 0) {
            printf("x is positive")
        }
        
        check x > 0
        
        def greet(name string) {
            printf("Hello,", name)
        }
        
        void main() {
            greet("World")
            printf("Type of x:", typeof(x))
        }
        
        # Test collections
        numbers := [1, 2, 3, 4, 5]
        data := {name: "John", age: 30}
    """.trimIndent()
    
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
}