# Goo.app - macOS Application Bundle

This is a macOS application bundle that wraps the `go` binary to provide Goo language compilation and execution.

## Installation

### Automatic Installation (Recommended)
```bash
./install-goo-app.sh
```

This script will:
- Remove quarantine attributes
- Copy Goo.app to /Applications
- Register with Launch Services
- Create command-line symlink
- Fix permissions

### Manual Installation
1. Copy `Goo.app` to your `/Applications` folder
2. Remove quarantine: `xattr -r -d com.apple.quarantine /Applications/Goo.app`
3. Register with Launch Services: `/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -f /Applications/Goo.app`
4. Create symlink: `sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo`

### Fixing File Association Issues

If you get "Finder does not have permission to open" errors:

1. **Remove Quarantine** (most common fix):
   ```bash
   xattr -r -d com.apple.quarantine /path/to/file.goo
   ```

2. **Set Default App**:
   - Right-click the .goo file → "Get Info"
   - Under "Open with", select your text editor (VS Code, TextEdit, etc.)
   - Click "Change All..."

3. **Re-register the app**:
   ```bash
   /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -f /Applications/Goo.app
   ```

## Usage

### Command Line
```bash
# Run a Goo program
./Goo.app/Contents/MacOS/goo run program.goo

# Build a Goo program  
./Goo.app/Contents/MacOS/goo build program.goo

# Pass through to go binary
./Goo.app/Contents/MacOS/goo version
```

### Adding to PATH
To use `goo` from anywhere:
```bash
sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo
```

## Features

- **Preprocessing**: Converts Goo syntax to Go before compilation
- **File Association**: Registers as handler for .goo files
- **Goo Syntax Support**:
  - `#` comments → `//` comments
  - `and`/`or` → `&&`/`||`
  - `not`/`¬` → `!`
  - `≠` → `!=`
  - `ø` → `nil`
  - `printf` → `fmt.Println`

## Requirements

- macOS 10.15 or later
- Go programming language installed
- Goo source files (.goo extension)

## Icon

The app includes an SVG icon. For a proper macOS icon, convert the SVG to a high-resolution PNG and then to .icns format using:
```bash
iconutil -c icns icon.iconset
```