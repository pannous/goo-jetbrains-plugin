#!/bin/bash

# Complete cleanup and proper installation of Goo tools

echo "🧹 Complete Goo cleanup and reinstallation..."

echo "🗑️  Removing old Goo.app installations..."
# Try to remove old app (may need sudo)
if [ -d "/Applications/Goo.app" ]; then
    if rm -rf /Applications/Goo.app 2>/dev/null; then
        echo "✅ Removed old Goo.app"
    else
        echo "⚠️  Need admin rights to remove /Applications/Goo.app"
        echo "Please run: sudo rm -rf /Applications/Goo.app"
        echo "Then run this script again."
        exit 1
    fi
fi

echo "🔄 Resetting Launch Services..."
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -kill -r -domain local -domain system -domain user

echo "📦 Installing Goo.app as command-line tool only..."
if cp -R Goo.app /Applications/ 2>/dev/null; then
    echo "✅ Installed Goo.app"
else
    echo "⚠️  Need permissions to install to /Applications"
    echo "Please run: sudo cp -R Goo.app /Applications/"
    echo "Then: sudo chown -R root:wheel /Applications/Goo.app"
fi

echo "🔗 Setting up command-line access..."
if [ -f "/usr/local/bin/goo" ]; then
    rm -f /usr/local/bin/goo 2>/dev/null || echo "Need sudo to remove old symlink"
fi

# Create symlink (may need sudo)
if ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo 2>/dev/null; then
    echo "✅ Created command-line symlink"
else
    echo "⚠️  Need admin rights for symlink"
    echo "Please run: sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo"
fi

echo ""
echo "🧪 Testing installation..."
if /Applications/Goo.app/Contents/MacOS/goo --help >/dev/null 2>&1; then
    echo "✅ Goo compiler works!"
else
    echo "❌ Goo compiler test failed"
fi

echo ""
echo "📝 For .goo files, use these approaches:"
echo "✅ Command line: goo run /path/to/file.goo"
echo "✅ Text editor: open -a TextEdit /path/to/file.goo" 
echo "✅ Set default: Right-click .goo file → Get Info → Open with → TextEdit → Change All"
echo ""
echo "❌ DON'T use: open -a Goo /path/to/file.goo (this causes error -54)"

echo ""
echo "🎉 Cleanup complete! Goo is now properly installed as a command-line tool."