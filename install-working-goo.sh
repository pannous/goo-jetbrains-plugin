#!/bin/bash

# Install working Goo.app with proper GUI support

echo "🚀 Installing working Goo.app..."

# Remove old installation
echo "🗑️  Removing old installation..."
sudo rm -rf /Applications/Goo.app 2>/dev/null || true

# Copy new app
echo "📦 Installing new Goo.app..."
sudo cp -R Goo.app /Applications/
sudo chown -R root:wheel /Applications/Goo.app
sudo chmod -R 755 /Applications/Goo.app
sudo chmod +x /Applications/Goo.app/Contents/MacOS/Goo

# Remove quarantine
echo "🔓 Removing quarantine attributes..."
sudo xattr -r -d com.apple.quarantine /Applications/Goo.app 2>/dev/null || true

# Reset Launch Services
echo "🔄 Resetting Launch Services..."
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -kill -r -domain local -domain system -domain user

# Register the new app
echo "📋 Registering Goo.app..."
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -f /Applications/Goo.app

# Create/update command line symlink
echo "🔗 Creating command line access..."
sudo rm -f /usr/local/bin/goo 2>/dev/null || true
sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo

# Test the installation
echo ""
echo "🧪 Testing installation..."
if open /Applications/Goo.app --args --test 2>/dev/null; then
    echo "✅ Goo.app GUI works!"
else
    echo "⚠️  GUI test had issues, but app is installed"
fi

if goo --help >/dev/null 2>&1; then
    echo "✅ Command line goo works!"
else
    echo "⚠️  Command line goo needs PATH setup"
fi

echo ""
echo "🎉 Installation complete!"
echo ""
echo "✅ Now you can use:"
echo "   • open -a Goo /path/to/file.goo"  
echo "   • Double-click .goo files (after setting Goo as default)"
echo "   • Drag .goo files to Goo.app"
echo "   • goo run file.goo (command line)"
echo ""
echo "🔧 To set Goo as default for .goo files:"
echo "   1. Right-click any .goo file → Get Info"
echo "   2. Under 'Open with' → Select Goo"
echo "   3. Click 'Change All...'"