#!/bin/bash

# Enhanced Goo Plugin Development Script
# Automates build, install, and restart workflow

set -e  # Exit on any error

echo "🔄 Building Goo IntelliJ Plugin v$(grep 'version = ' build.gradle.kts | cut -d'"' -f2)..."
gradle clean buildPlugin

echo "📦 Installing plugin to GoLand..."
PLUGIN_DIR="/Users/me/Library/Application Support/JetBrains/GoLand2025.1/plugins"
PLUGIN_NAME="goo-intellij"

# Remove old plugin
if [ -d "$PLUGIN_DIR/$PLUGIN_NAME" ]; then
    echo "🗑️  Removing old plugin version..."
    rm -rf "$PLUGIN_DIR/$PLUGIN_NAME"
fi

# Copy new plugin
cp -r ".sandbox/GO-2025.1.3/plugins/$PLUGIN_NAME" "$PLUGIN_DIR/"

# Update soft links
echo "🔗 Updating project soft links..."
rm -f goo-intellij-plugin.jar goo-intellij-plugin-dir
ln -s build/libs/goo-intellij-*.jar goo-intellij-plugin.jar
ln -s .sandbox/GO-2025.1.3/plugins/goo-intellij goo-intellij-plugin-dir

echo "✅ Plugin updated successfully!"

# Check if GoLand is running
if pgrep -f "GoLand" > /dev/null; then
    echo "⚠️  GoLand is currently running"
    read -p "🔄 Restart GoLand to load new plugin? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🔄 Restarting GoLand..."
        
        # Kill GoLand gracefully
        osascript -e 'tell application "GoLand" to quit'
        sleep 3
        
        # Force kill if still running
        pkill -f GoLand 2>/dev/null || true
        sleep 2
        
        # Start GoLand
        echo "🚀 Starting GoLand..."
        open -a "GoLand"
        
        echo "✅ GoLand restarted with new plugin!"
    else
        echo "ℹ️  Manual restart required: GoLand → Preferences → Plugins → Disable/Enable 'Goo Language Support'"
    fi
else
    echo "ℹ️  GoLand not running. Plugin will be loaded on next startup."
fi

echo "🎉 Development cycle complete!"
echo "📍 Plugin JAR: $(readlink goo-intellij-plugin.jar)"
echo "📍 Plugin Dir: $(readlink goo-intellij-plugin-dir)"