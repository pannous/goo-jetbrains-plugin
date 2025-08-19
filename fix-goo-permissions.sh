#!/bin/bash

# Quick fix for .goo file opening issues

set -e

echo "🔧 Fixing .goo file permissions and associations..."

# Check if file argument is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <file.goo> [file2.goo ...]"
    echo "   or: $0 *.goo"
    exit 1
fi

# Process each file
for file in "$@"; do
    if [[ "$file" == *.goo ]]; then
        echo "🔓 Fixing permissions for: $file"
        
        # Remove quarantine
        xattr -d com.apple.quarantine "$file" 2>/dev/null || true
        
        # Set proper permissions
        chmod 644 "$file"
        
        echo "✅ Fixed: $file"
    else
        echo "⚠️  Skipping non-.goo file: $file"
    fi
done

echo ""
echo "🎉 All done! You can now:"
echo "  - Double-click .goo files to open with text editor"
echo "  - Or right-click → 'Open With' → choose your preferred editor"
echo "  - Use 'goo run file.goo' from Terminal to compile and run"
echo ""
echo "💡 To set default app for all .goo files:"
echo "  1. Right-click any .goo file → 'Get Info'"
echo "  2. Under 'Open with', select your text editor"
echo "  3. Click 'Change All...'"