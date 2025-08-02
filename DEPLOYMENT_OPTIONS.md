# Goo Compiler Deployment Options

## Current Implementation: Configurable Paths ✅

The plugin now supports:
- **Auto-detection**: Searches common installation paths
- **Manual configuration**: Settings UI to specify compiler location  
- **Fallback**: Uses system PATH if no specific path configured
- **Toggle**: Can disable compiler integration entirely

### Common Install Locations Checked:
- `/opt/other/go/bin/go` (our development setup)
- `/usr/local/go/bin/go` (standard Unix install)
- `/usr/local/goo/bin/go` (Goo-specific install)
- `~/go/bin/go` (user install)
- `~/goo/bin/go` (user Goo install)
- `go` (system PATH)

## Alternative: Bundle Compiler 📦

### Pros:
- ✅ Zero configuration required
- ✅ Always works out of the box
- ✅ Consistent compiler version
- ✅ No user setup required

### Cons:
- ❌ Larger plugin download size (~50-100MB)
- ❌ Platform-specific builds (Windows/Mac/Linux)
- ❌ More complex build process
- ❌ Harder to update compiler independently

### Implementation Approach:
```
src/main/resources/
  compilers/
    darwin-arm64/
      go (executable)
    darwin-amd64/  
      go (executable)
    linux-amd64/
      go (executable)
    windows-amd64/
      go.exe (executable)
```

## Alternative: Download on Demand 📥

### Implementation:
- Plugin detects missing compiler
- Shows notification: "Goo compiler not found. Download?"
- Downloads appropriate binary for user's platform
- Installs to plugin's data directory

### Pros:
- ✅ Small initial download
- ✅ Zero configuration
- ✅ Always up-to-date compiler
- ✅ Platform-appropriate binary

### Cons:
- ❌ Requires internet connection
- ❌ More complex update mechanism
- ❌ Security considerations (downloading executables)

## Recommendation: Current Approach 🎯

The **configurable paths approach** is best because:

1. **Flexible**: Works with any Goo installation
2. **Lightweight**: No bundled binaries
3. **Simple**: Straightforward implementation  
4. **Safe**: No automatic downloads
5. **Developer-friendly**: Easy to point to custom builds

Users can:
- Install Goo compiler anywhere they want
- Use development/custom builds
- Configure via intuitive settings UI
- Disable integration if desired

## User Instructions

### For End Users:
1. Install Goo compiler from: https://github.com/pannous/goo/releases
2. Open GoLand → Preferences → Languages & Frameworks → Goo Compiler
3. Set compiler path (auto-detected in most cases)
4. Enable compiler integration

### For Development Setup:
The plugin auto-detects our development setup at `/opt/other/go/bin/go`.