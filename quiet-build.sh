#!/bin/bash

# Quiet build script that suppresses verbose output
# Only shows essential information and errors

set -e

echo "🔧 Building Goo IntelliJ Plugin..."

# Clean previous build to force version update  
rm -rf build/libs/*.jar 2>/dev/null || true

# Run gradle build and capture only essential output  
gradle clean buildPlugin --quiet 2>&1 | grep -E "(BUILD|FAILED|ERROR|WARN.*deprecated)" || true

# Check build result
BUILD_RESULT=${PIPESTATUS[0]}

if [ $BUILD_RESULT -eq 0 ]; then
    echo "✅ Build completed successfully"
    
    # Show the actual JAR version that was built
    JAR_FILE=$(ls build/libs/goo-*.jar 2>/dev/null | head -1)
    if [ -n "$JAR_FILE" ]; then
        VERSION=$(basename "$JAR_FILE" .jar | sed 's/goo-//')
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo "📦 Built version: $VERSION ($JAR_SIZE)"
    fi
else
    echo "❌ Build failed"
    exit 1
fi