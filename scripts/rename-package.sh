#!/usr/bin/env bash
# Renames the app's Java package and applicationId/namespace throughout the project.
# Usage: ./scripts/rename-package.sh com.yourcompany.yourapp
set -euo pipefail

OLD_PACKAGE="com.monstertechno.webview"
NEW_PACKAGE="${1:-}"

if [[ -z "$NEW_PACKAGE" ]]; then
  echo "Usage: $0 <new.package.name>"
  echo "Example: $0 com.acme.myapp"
  exit 1
fi

if [[ ! "$NEW_PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Error: '$NEW_PACKAGE' is not a valid Java package name (lowercase, dot-separated, e.g. com.acme.myapp)"
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

OLD_PATH="${OLD_PACKAGE//./\/}"
NEW_PATH="${NEW_PACKAGE//./\/}"

echo "Renaming package: $OLD_PACKAGE -> $NEW_PACKAGE"

for SRC_ROOT in app/src/main/java app/src/test/java app/src/androidTest/java; do
  OLD_DIR="$SRC_ROOT/$OLD_PATH"
  if [[ -d "$OLD_DIR" ]]; then
    NEW_DIR="$SRC_ROOT/$NEW_PATH"
    mkdir -p "$(dirname "$NEW_DIR")"
    git mv "$OLD_DIR" "$NEW_DIR" 2>/dev/null || mv "$OLD_DIR" "$NEW_DIR"
    echo "Moved $OLD_DIR -> $NEW_DIR"
  fi
done

# Update package/import declarations in all moved Java files
find app/src -type f -name "*.java" -print0 | xargs -0 sed -i '' "s/${OLD_PACKAGE//./\\.}/${NEW_PACKAGE}/g"

# Update namespace and applicationId in build.gradle.kts
sed -i '' "s/\"${OLD_PACKAGE//./\\.}\"/\"${NEW_PACKAGE}\"/g" app/build.gradle.kts

# Clean up now-empty old package directories
for SRC_ROOT in app/src/main/java app/src/test/java app/src/androidTest/java; do
  find "$SRC_ROOT" -type d -empty -delete 2>/dev/null || true
done

echo ""
echo "Done. Review the changes with 'git diff', then run:"
echo "  ./gradlew assembleDebug"
echo "to verify the project still builds."
