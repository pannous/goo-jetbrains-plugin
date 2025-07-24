# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a JetBrains IDE plugin for the Goo programming language, a Go dialect with enhanced syntax. The plugin provides syntax highlighting, code completion, run configurations, and other IDE features for .goo files.

## Build Commands

### Development Workflow
```bash
# Quick build, install and restart GoLand (recommended)
./update-plugin.sh

# Manual build only
gradle buildPlugin

# Version increment (patch/minor/major)
./increment-version.sh
```

### Plugin Installation
The `runIde` gradle task is broken due to IntelliJ Platform Gradle Plugin 2.x issues. Use manual installation:

```bash
# Copy built plugin to GoLand plugins directory
cp -r "build/idea-sandbox/GO-2025.1.3/plugins/goo-intellij" "/Users/me/Library/Application Support/JetBrains/GoLand2025.1/plugins/"
```

Or install via Plugin Manager: GoLand → Preferences → Plugins → Gear Icon → "Install Plugin from Disk..." → Select `build/libs/goo-intellij-*.jar`

### Testing
Test files are available: `lexer_test.goo`, `simple_test.goo`, `comprehensive_test.goo`

After plugin installation:
1. Open test file in GoLand
2. Verify syntax highlighting works
3. Test right-click "Run Goo File" action
4. Check code folding and completion

## Architecture

### Core Components
- **Language Definition**: `GooLanguage.kt` - Main language object
- **File Type**: `GooFileType.kt` - .goo file recognition
- **Lexer**: `lexer/GooLexer.kt`, `GooLexerAdapter.kt` - Tokenization
- **Parser**: `parser/GooParser.kt` - Simple parser that accepts any token sequence
- **PSI**: `psi/GooFile.kt` - Program Structure Interface

### IDE Features
- **Syntax Highlighting**: `highlighting/GooSyntaxHighlighter.kt`
- **Code Completion**: `completion/GooCompletionContributor.kt`
- **Code Folding**: `folding/GooFoldingBuilder.kt`
- **Run Configurations**: `run/` package with configuration types and runners
- **Actions**: `actions/` package for new file creation and run actions
- **Settings**: `settings/GooColorSettingsPage.kt` for color customization

### Plugin Configuration
- `src/main/resources/META-INF/plugin.xml` - Main plugin descriptor
- `build.gradle.kts` - Gradle build configuration for IntelliJ platform
- Targets GoLand 2025.1.3 with compatibility for build 251.*

## Goo Language Features

The plugin supports Goo syntax extensions over Go:
- `#` comments and shebang support
- `and`/`or` operators instead of `&&`/`||`
- `¬`/`not` operator instead of `!`
- `≠` operator instead of `!=`
- `ø` keyword instead of `nil`
- Truthy/falsey if statements
- No main function requirement
- `printf` as synonym for `fmt.Println`

## Development Notes

- Uses Kotlin with Java 21 target
- IntelliJ Platform Gradle Plugin 2.1.0
- Plugin ID: `com.pannous.goo`
- Version managed in `gradle.properties` and `build.gradle.kts`
- Icons and file templates in `src/main/resources/`

## Common Issues

- `gradle runIde` task fails - use manual plugin installation instead
- Always restart GoLand completely for plugin.xml or structural changes
- Use disable/enable in Plugin Manager for quick code-only changes