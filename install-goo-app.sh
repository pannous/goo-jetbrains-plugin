#!/bin/bash

# Install Goo.app and fix macOS permissions/quarantine issues

set -e

echo "🚀 Installing Goo.app..."

# Check if we're in the right directory
if [ ! -d "Goo.app" ]; then
    echo "❌ Error: Goo.app not found in current directory"
    echo "Please run this script from the goo-jetbrains-plugin directory"
    exit 1
fi

# Remove quarantine attributes
echo "🔓 Removing quarantine attributes..."
sudo xattr -r -d com.apple.quarantine Goo.app 2>/dev/null || true

# Make sure the executable is properly set
echo "🔧 Setting executable permissions..."
chmod +x Goo.app/Contents/MacOS/goo

# Copy to Applications if it doesn't exist there
if [ ! -d "/Applications/Goo.app" ]; then
    echo "📦 Copying Goo.app to /Applications..."
    sudo cp -R Goo.app /Applications/
    sudo chown -R root:wheel /Applications/Goo.app
else
    echo "📦 Updating existing Goo.app in /Applications..."
    sudo rm -rf /Applications/Goo.app
    sudo cp -R Goo.app /Applications/
    sudo chown -R root:wheel /Applications/Goo.app
fi

# Remove quarantine from the installed app
echo "🔓 Removing quarantine from installed app..."
sudo xattr -r -d com.apple.quarantine /Applications/Goo.app 2>/dev/null || true

# Register the app with Launch Services
echo "📋 Registering with Launch Services..."
/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -f /Applications/Goo.app

# Create symlink for command line usage
if [ ! -f "/usr/local/bin/goo" ]; then
    echo "🔗 Creating command line symlink..."
    sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo
else
    echo "🔗 Updating command line symlink..."
    sudo rm -f /usr/local/bin/goo
    sudo ln -sf /Applications/Goo.app/Contents/MacOS/goo /usr/local/bin/goo
fi

# Test the installation
echo "🧪 Testing installation..."
if /Applications/Goo.app/Contents/MacOS/goo --help >/dev/null 2>&1; then
    echo "✅ Goo.app installed successfully!"
    echo ""
    echo "Usage:"
    echo "  - Double-click .goo files to open with default text editor"
    echo "  - Use 'goo run file.goo' from Terminal"
    echo "  - Use 'goo build file.goo' from Terminal"
    echo ""
    echo "To fix file associations:"
    echo "  1. Right-click a .goo file"
    echo "  2. Select 'Get Info'"
    echo "  3. Under 'Open with', select your preferred text editor"
    echo "  4. Click 'Change All...'"
else
    echo "❌ Installation failed - please check permissions"
    exit 1
fi