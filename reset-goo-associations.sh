#!/bin/bash

# Comprehensive fix for .goo file association issues

set -e

echo "🔄 Resetting .goo file associations and permissions..."

# Function to fix a single file
fix_file() {
    local file="$1"
    echo "🔧 Processing: $file"
    
    # Remove ALL extended attributes
    xattr -c "$file" 2>/dev/null || true
    
    # Set standard permissions
    chmod 644 "$file"
    
    # Check if attributes are really gone
    local attrs=$(xattr "$file" 2>/dev/null || true)
    if [ -z "$attrs" ]; then
        echo "✅ Cleaned: $file"
    else
        echo "⚠️  Still has attributes: $attrs"
    fi
}

# Process command line arguments
if [ $# -eq 0 ]; then
    echo "Usage: $0 <file.goo> [file2.goo ...]"
    echo "   or: $0 /path/to/directory/*.goo"
    exit 1
fi

# Process each file
for file in "$@"; do
    if [ -f "$file" ] && [[ "$file" == *.goo ]]; then
        fix_file "$file"
    elif [ -f "$file" ]; then
        echo "⚠️  Not a .goo file: $file"
    else
        echo "❌ File not found: $file"
    fi
done

echo ""
echo "🔄 Resetting Launch Services database..."
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -kill -r -domain local -domain system -domain user

echo ""
echo "🎯 Setting .goo files to open with TextEdit by default..."
# Use duti to set default app if available
if command -v duti &> /dev/null; then
    duti -s com.apple.TextEdit .goo all
    echo "✅ Set TextEdit as default for .goo files"
else
    echo "💡 Install 'duti' for automatic file association: brew install duti"
fi

echo ""
echo "🎉 Reset complete! Now try:"
echo "  1. Double-click a .goo file - it should open in TextEdit"
echo "  2. If not, right-click → Get Info → Change 'Open with' to TextEdit → Change All"
echo "  3. Use 'goo run file.goo' from Terminal to compile and run"

echo ""
echo "🔍 Testing file associations..."
for file in "$@"; do
    if [ -f "$file" ] && [[ "$file" == *.goo ]]; then
        echo "File: $file"
        echo "  Attributes: $(xattr "$file" 2>/dev/null || echo 'none')"
        echo "  Default app: $(mdls -name kMDItemContentTypeTree -name kMDItemContentType "$file" 2>/dev/null || echo 'unknown')"
    fi
done