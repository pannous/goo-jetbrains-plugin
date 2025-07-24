#!/bin/bash

# Version increment script for Goo IntelliJ Plugin

set -e

GRADLE_FILE="build.gradle.kts"

# Get current version
CURRENT_VERSION=$(grep 'version = ' $GRADLE_FILE | cut -d'"' -f2)
echo "📋 Current version: $CURRENT_VERSION"

# Parse version components
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]:-0}  # Default to 0 if not present

echo "🔍 Parsed: Major=$MAJOR, Minor=$MINOR, Patch=$PATCH"

# Increment options
echo "🚀 Select version increment:"
echo "1) Patch ($MAJOR.$MINOR.$((PATCH + 1))) - Bug fixes"
echo "2) Minor ($MAJOR.$((MINOR + 1)).0) - New features"
echo "3) Major ($((MAJOR + 1)).0.0) - Breaking changes"
echo "4) Custom version"

read -p "Enter choice (1-4): " -n 1 -r
echo

case $REPLY in
    1)
        NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))"
        ;;
    2)
        NEW_VERSION="$MAJOR.$((MINOR + 1)).0"
        ;;
    3)
        NEW_VERSION="$((MAJOR + 1)).0.0"
        ;;
    4)
        read -p "Enter custom version: " NEW_VERSION
        ;;
    *)
        echo "❌ Invalid choice. Exiting."
        exit 1
        ;;
esac

echo "📝 Updating version to: $NEW_VERSION"

# Update version in build.gradle.kts
sed -i '' "s/version = \"$CURRENT_VERSION\"/version = \"$NEW_VERSION\"/" $GRADLE_FILE

# Verify the change
UPDATED_VERSION=$(grep 'version = ' $GRADLE_FILE | cut -d'"' -f2)
if [ "$UPDATED_VERSION" = "$NEW_VERSION" ]; then
    echo "✅ Version successfully updated to: $NEW_VERSION"
    
    # Ask if user wants to commit the change
    read -p "💾 Commit version bump? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git add $GRADLE_FILE
        git commit -m "chore(version): bump version to $NEW_VERSION"
        echo "✅ Version bump committed"
    fi
    
    # Ask if user wants to build and update plugin
    read -p "🔨 Build and update plugin now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ./update-plugin.sh
    fi
else
    echo "❌ Failed to update version. Please check $GRADLE_FILE manually."
    exit 1
fi