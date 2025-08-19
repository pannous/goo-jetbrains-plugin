#!/bin/bash

# Final solution for .goo file opening issues

echo "🚀 Final fix for .goo file opening..."

# Step 1: Clean the specific file
echo "📁 Cleaning file attributes..."
if [ $# -ge 1 ]; then
    for file in "$@"; do
        if [ -f "$file" ]; then
            echo "  Processing: $file"
            xattr -c "$file" 2>/dev/null || true
            chmod 644 "$file"
        fi
    done
else
    echo "Usage: $0 <file.goo> [additional files...]"
    exit 1
fi

echo ""
echo "🔄 Resetting file associations..."
# Reset Launch Services
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -kill -r -domain local -domain system -domain user

echo ""
echo "📝 Manual file association steps:"
echo "1. Right-click your .goo file"
echo "2. Select 'Get Info'"
echo "3. Under 'Open with:', click the dropdown"
echo "4. Select 'TextEdit' (or your preferred editor)"
echo "5. Click 'Change All...'"
echo "6. Click 'Continue' to apply to all .goo files"

echo ""
echo "🎯 Alternative: Use 'open' command directly:"
echo "   open -a TextEdit /opt/other/go/probes/test_power_basic.goo"

echo ""
echo "⚡ For compilation, use the command line:"
echo "   goo run /opt/other/go/probes/test_power_basic.goo"

echo ""
echo "✅ Files processed. Try double-clicking your .goo file now!"