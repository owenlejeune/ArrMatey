#!/usr/bin/env bash

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

BUILD_DIR="build"
ARCHIVE_PATH="$BUILD_DIR/iosApp.xcarchive"
IPA_PATH="$BUILD_DIR/ArrMatey.ipa"

echo "=== Building Unsigned iOS IPA for ArrMatey ==="

# 1. Clean previous build directory
echo "Cleaning build directory..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# 2. Archive for iOS device (iphoneos, arm64) without code signing
echo "Archiving iOS Release build..."
xcodebuild archive \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -sdk iphoneos \
  -destination 'generic/platform=iOS' \
  -archivePath "$ARCHIVE_PATH" \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGN_ALLOWED=NO

# 3. Package into Payload structure
echo "Packaging .app into Payload..."
mkdir -p "$BUILD_DIR/Payload"
cp -r "$ARCHIVE_PATH/Products/Applications/ArrMatey.app" "$BUILD_DIR/Payload/"

# 4. Create IPA archive
echo "Creating ArrMatey.ipa..."
cd "$BUILD_DIR"
zip -r "ArrMatey.ipa" Payload > /dev/null
rm -rf Payload

cd "$PROJECT_ROOT"

echo "=== Build Complete ==="
echo "IPA created successfully at: $IPA_PATH"
