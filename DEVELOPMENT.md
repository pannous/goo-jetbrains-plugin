# Goo IntelliJ Plugin Development Guide

## 🚀 Quick Development Workflow

### Option 1: Automated Script (Recommended)
```bash
# Build, install, and restart GoLand with new plugin
./update-plugin.sh
```

### Option 2: Manual Steps
```bash
# Build plugin
gradle buildPlugin

# Install to GoLand
cp -r "build/idea-sandbox/GO-2025.1.3/plugins/goo-intellij" "/Users/me/Library/Application Support/JetBrains/GoLand2025.1/plugins/"

# Restart GoLand manually
```

### Option 3: Plugin Manager Installation
1. **Build**: `gradle buildPlugin`
2. **GoLand → Preferences → Plugins**
3. **Gear Icon → "Install Plugin from Disk..."**
4. **Select**: `build/libs/goo-intellij-1.1.0.jar`
5. **Restart IDE**

## 📦 Version Management

### Increment Version
```bash
./increment-version.sh
```

Options:
- **Patch** (1.1.0 → 1.1.1): Bug fixes
- **Minor** (1.1.0 → 1.2.0): New features  
- **Major** (1.1.0 → 2.0.0): Breaking changes
- **Custom**: Enter any version

## 🐛 Known Issues

### ❌ `runIde` Task Error
```
Execution failed for task ':runIde'.
> Index: 1, Size: 1
```

**Root Cause**: IntelliJ Platform Gradle Plugin 2.x has issues with IDE home variable resolution when using local IDE installations.

**Workaround**: Use manual plugin installation instead of `gradle runIde`.

**Alternatives Tried**:
- ✅ Manual plugin copying (works)
- ✅ Plugin Manager installation (works)
- ❌ `gradle runIde` (broken)
- ❌ Local IDE path configuration (broken)

## 🎯 Testing

### Test Files
- **`lexer_test.goo`**: Basic lexer functionality
- **`simple_test.goo`**: Core language features
- **`comprehensive_test.goo`**: All Goo language features

### Testing Procedure
1. **Install plugin** using `./update-plugin.sh`
2. **Open test file** in GoLand
3. **Verify**:
   - Syntax highlighting works
   - Right-click "Run Goo File" appears
   - Run configurations work
   - Code folding functions
   - Auto-completion activates

## 📁 Project Structure

```
goo-intellij/
├── src/main/kotlin/com/pannous/goo/
│   ├── actions/          # IDE actions (run, new file)
│   ├── completion/       # Code completion
│   ├── folding/          # Code folding
│   ├── highlighting/     # Syntax highlighting
│   ├── lexer/           # Lexical analysis
│   ├── parser/          # Parsing
│   ├── psi/             # Program Structure Interface
│   ├── run/             # Run configurations
│   └── settings/        # IDE settings pages
├── src/main/resources/
│   ├── META-INF/        # Plugin configuration
│   ├── icons/           # File type icons
│   └── fileTemplates/   # New file templates
├── build/libs/          # Built JAR files
└── goo-intellij-plugin.jar → Symlink to latest JAR
```

## 🔧 Development Tips

1. **Always use `./update-plugin.sh`** for fastest development cycle
2. **Increment version** before major changes using `./increment-version.sh`
3. **Test thoroughly** with all `.goo` test files after changes
4. **Restart GoLand completely** for plugin.xml or major structural changes
5. **Use disable/enable** in Plugin Manager for quick code-only changes

## 🚦 Plugin Status

- ✅ **Syntax Highlighting**: Full Goo language support
- ✅ **File Type Recognition**: `.goo` files properly handled
- ✅ **Run Configurations**: Right-click execution works
- ✅ **Code Completion**: Goo keywords supported
- ✅ **Code Folding**: Comments and functions foldable
- ✅ **Plugin Icons**: Custom branding throughout IDE
- ❌ **IDE Development Mode**: `runIde` task broken