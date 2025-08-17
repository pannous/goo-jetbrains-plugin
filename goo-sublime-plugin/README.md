# Goo Sublime Text Plugin

A Sublime Text syntax highlighting plugin for the Goo programming language.

## Installation

1. Copy the `goo-sublime-plugin` folder to your Sublime Text Packages directory:
   - **macOS**: `~/Library/Application Support/Sublime Text/Packages/`
   - **Windows**: `%APPDATA%/Sublime Text/Packages/`
   - **Linux**: `~/.config/sublime-text/Packages/`

2. Restart Sublime Text

## Features

- Syntax highlighting for `.goo` files
- Support for Goo-specific syntax:
  - `#` and `//` comments
  - `and`/`or` operators
  - `¬`/`not` operator
  - `≠` operator
  - `ø` keyword (nil equivalent)
  - `printf` keyword
- Built on Go syntax foundation
- Custom color theme optimized for Goo syntax

## Usage

Open any `.goo` file in Sublime Text and the syntax highlighting will be applied automatically.

To manually set the syntax: View → Syntax → Goo

### Comment Toggle

- **Ctrl+/** (Cmd+/ on Mac): Toggle line comments using `//`
- **Ctrl+Shift+/** (Cmd+Shift+/ on Mac): Toggle block comments using `/* */`

The plugin supports both `//` and `#` comment styles for Goo files.

## Goo Language Features

This plugin supports syntax highlighting for Goo's enhanced Go syntax including:
- Enhanced comment support (`#` in addition to `//`)
- Logical operators: `and`, `or`, `not`, `¬`
- Comparison operators: `≠`
- Nil equivalent: `ø`
- Print function: `printf`