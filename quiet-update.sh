#!/bin/bash

# Quiet plugin update script
# Builds and installs plugin with minimal output

set -e

PLUGIN_NAME="goo-intellij"
PLUGIN_DIR="/Users/me/Library/Application Support/JetBrains/GoLand2025.1/plugins"

echo "🔄 Building and updating Goo plugin..."

# Quick clean build - show output for debugging
echo "🔧 Building..."
./quiet-build.sh

# Update plugin quietly
if [ -d "$PLUGIN_DIR/$PLUGIN_NAME" ]; then
    rm -rf "$PLUGIN_DIR/$PLUGIN_NAME" 2>/dev/null
fi

# Install from sandbox (check both possible locations)
if [ -d "build/idea-sandbox/GO-2025.1.3/plugins/goo" ]; then
    cp -r "build/idea-sandbox/GO-2025.1.3/plugins/goo" "$PLUGIN_DIR/$PLUGIN_NAME" 2>/dev/null
    echo "✅ Plugin installed successfully"
elif [ -d "build/idea-sandbox/GO-2025.1.3/plugins/$PLUGIN_NAME" ]; then
    cp -r "build/idea-sandbox/GO-2025.1.3/plugins/$PLUGIN_NAME" "$PLUGIN_DIR/" 2>/dev/null
    echo "✅ Plugin installed successfully"
else
    echo "❌ Plugin build failed - sandbox not found"
    exit 1
fi

# Update soft links quietly
rm -f goo-plugin.jar goo-plugin-dir 2>/dev/null
ln -s build/libs/goo-*.jar goo-plugin.jar 2>/dev/null || true
ln -s "build/idea-sandbox/GO-2025.1.3/plugins/$PLUGIN_NAME" goo-plugin-dir 2>/dev/null || true

# Check GoLand status
if pgrep -f "GoLand" > /dev/null 2>&1; then
    # echo "⚠️  Please restart GoLand to load the updated plugin"
    echo "🔄 Restarting GoLand..."
        
        # Kill GoLand gracefully
        osascript -e 'tell application "GoLand" to quit' || true
        sleep 3
        
        # Force kill if still running
        pkill -f GoLand 2>/dev/null || true
        sleep 2
        
        # Start GoLand
        echo "🚀 Starting GoLand..."
        open -a "GoLand"
else
    echo "🚀 Ready to launch GoLand with updated plugin"
    open -a "GoLand"
fi

