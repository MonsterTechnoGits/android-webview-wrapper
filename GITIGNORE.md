# Git Ignore Configuration

This project uses a comprehensive `.gitignore` setup optimized for Android development with WebView components.

## Structure

### Root `.gitignore`
Contains general Android development exclusions:
- Build outputs (APK, AAR, DEX files)
- IDE-specific files (Android Studio, IntelliJ IDEA)
- Generated files and temporary directories
- OS-specific files (macOS, Windows, Linux)
- Development tools (lint, profiling, testing)

### App Module `.gitignore` (`app/.gitignore`)
Contains app-specific exclusions:
- App build outputs
- Debug and test artifacts
- WebView cache and debugging files
- Placeholder for sensitive configuration files

## Key Files Ignored

### Build Outputs
```
*.apk          # Android Package files
*.aab          # Android App Bundle files
*.aar          # Android Archive files
build/         # All build directories
```

### IDE Files
```
*.iml          # IntelliJ module files
.idea/         # IntelliJ IDEA settings (partial)
.DS_Store      # macOS system files
```

### Development Files
```
*.log          # Log files
*.hprof        # Memory profiler files
local.properties  # Local SDK paths
```

## Important Notes

### Signing Files
**Keystore files are NOT ignored by default** to allow for development signing.
If you want to exclude production keystores, uncomment these lines:
```gitignore
#*.jks
#*.keystore
```

### Google Services
`google-services.json` is commented out. Uncomment if you don't want to track Firebase configuration:
```gitignore
# google-services.json
```

### Sensitive Data
WebView apps may contain sensitive configuration in assets. Use the commented sections in `app/.gitignore`:
```gitignore
# /src/main/assets/config/production/
# /src/main/assets/keys/
# /src/main/res/raw/api_keys*
```

## Customization

### For WebView-Specific Files
Add to `app/.gitignore`:
```gitignore
# Custom WebView configurations
/src/main/assets/private/
/src/main/assets/config/local/
*.key
*.certificate
```

### For Production Configurations
```gitignore
# Production settings
/src/main/assets/config/prod/
/src/main/res/raw/prod_*
app-release.apk
```

### For Development Tools
```gitignore
# Development utilities
debug/
tools/
scripts/local/
```

## Verification

After updating `.gitignore`, clean up already-tracked files that should be ignored:

```bash
# Remove cached build directories
git rm -r --cached build app/build

# Remove IDE files
git rm -r --cached .idea/caches .idea/libraries

# Remove log files
git rm --cached *.log

# Add updated .gitignore
git add .gitignore app/.gitignore

# Commit changes
git commit -m "Update .gitignore configuration"
```

## Best Practices

1. **Review regularly**: Update `.gitignore` as the project evolves
2. **Test locally**: Ensure important files aren't accidentally ignored
3. **Document exceptions**: Comment any unusual ignore patterns
4. **Use global gitignore**: Set up OS-specific ignores globally
5. **Secure sensitive data**: Never commit API keys, passwords, or certificates

## Global Git Configuration

Set up a global `.gitignore` for OS-specific files:

```bash
# Create global gitignore
git config --global core.excludesfile ~/.gitignore_global

# Add common OS files
echo ".DS_Store" >> ~/.gitignore_global
echo "Thumbs.db" >> ~/.gitignore_global
echo "*.swp" >> ~/.gitignore_global
```

This comprehensive setup ensures your WebView Android project maintains a clean repository while protecting sensitive information and excluding unnecessary build artifacts.