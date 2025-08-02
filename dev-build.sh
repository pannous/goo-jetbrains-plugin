#!/bin/bash

# Development build with auto-versioning
# Creates temporary version for testing without affecting release version

set -e

GRADLE_FILE="build.gradle.kts"
BACKUP_FILE="build.gradle.kts.backup"

# Backup current file
cp $GRADLE_FILE $BACKUP_FILE

# Get current version and git info
CURRENT_VERSION=$(grep 'version = ' $GRADLE_FILE | cut -d'"' -f2)
GIT_COMMIT_COUNT=$(git rev-list --count HEAD)
GIT_HASH=$(git rev-parse --short HEAD)

# Create development version
DEV_VERSION="${CURRENT_VERSION}-dev.${GIT_COMMIT_COUNT}.${GIT_HASH}"

echo "🔨 Creating development build: $DEV_VERSION"

# Temporarily update version
sed -i '' "s/version = \"$CURRENT_VERSION\"/version = \"$DEV_VERSION\"/" $GRADLE_FILE

# Build plugin
gradle buildPlugin

# Restore original version
mv $BACKUP_FILE $GRADLE_FILE

echo "✅ Development build complete: $DEV_VERSION"
echo "📦 Plugin JAR: build/libs/goo-intellij-$DEV_VERSION.jar"
echo "🔄 Original version restored: $CURRENT_VERSION"