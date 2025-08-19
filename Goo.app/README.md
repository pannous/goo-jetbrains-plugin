# Goo.app - macOS Application Bundle

This is a macOS application bundle that wraps the `go` binary to provide Goo language compilation and execution.

## Installation

1. Copy `Goo.app` to your `/Applications` folder
2. Make sure Go is installed on your system
3. You can now use `goo` commands from Terminal or by opening .goo files

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