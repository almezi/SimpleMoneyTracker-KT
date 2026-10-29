#!/bin/bash
# Builds a release .app for iOS and copies it to the release directory.
#
# Usage:
#   ./iosApp/release.sh                    # Release for simulator
#   ./iosApp/release.sh device            # Release for physical device (requires signing)
#   ./iosApp/release.sh archive          # Create .xcarchive for App Store distribution
#
# The Kotlin framework is built by the "Build Kotlin framework" run-script phase
# inside the Xcode project, which calls
#   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode

set -euo pipefail

export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
unset ANDROID_PREFS_ROOT

MODE="${1:-simulator}"
ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(dirname "$ROOT")"
PROJECT="$ROOT/iosApp.xcodeproj"
SCHEME="Saku"
DERIVED="$ROOT/build/DerivedData"
ARCHIVE_PATH="$ROOT/build/Saku.xcarchive"
EXPORT_PATH="$ROOT/build/Export"
RELEASE_ROOT="$REPO/release/ios"

# Sync version from Android (app/build.gradle.kts)
GRADLE_FILE="$REPO/app/build.gradle.kts"
INFO_PLIST="$ROOT/iosApp/Info.plist"
PBXPROJ="$PROJECT/project.pbxproj"

VERSION_NAME="$(sed -n 's/^        versionName = "\([^"]*\)"$/\1/p' "$GRADLE_FILE")"
VERSION_CODE="$(sed -n 's/^        versionCode = \([0-9]\{1,\}\)$/\1/p' "$GRADLE_FILE")"

if [ -z "$VERSION_NAME" ] || [ -z "$VERSION_CODE" ]; then
  echo "ERROR: Could not parse version from $GRADLE_FILE" >&2
  exit 1
fi

echo "==> Syncing iOS version with Android: $VERSION_NAME (build $VERSION_CODE)"

# Update Info.plist
/usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $VERSION_NAME" "$INFO_PLIST"
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $VERSION_CODE" "$INFO_PLIST"
echo "    Updated $INFO_PLIST"

# Update project.pbxproj (MARKETING_VERSION and CURRENT_PROJECT_VERSION)
sed -i '' "s/MARKETING_VERSION = [0-9.]*/MARKETING_VERSION = $VERSION_NAME/g" "$PBXPROJ"
sed -i '' "s/CURRENT_PROJECT_VERSION = [0-9.]*/CURRENT_PROJECT_VERSION = $VERSION_CODE/g" "$PBXPROJ"
echo "    Updated $PBXPROJ"

# Create release directory if it doesn't exist
mkdir -p "$RELEASE_ROOT"

# Versioned app bundle name
APP_NAME="Saku-v$VERSION_NAME.app"

case "$MODE" in
  simulator)
    echo "==> Building Release for iOS Simulator"
    xcodebuild \
      -project "$PROJECT" \
      -scheme "$SCHEME" \
      -configuration Release \
      -sdk iphonesimulator \
      -destination "platform=iOS Simulator,name=iPhone 18 Pro" \
      -derivedDataPath "$DERIVED" \
      ONLY_ACTIVE_ARCH=YES \
      build

    APP="$DERIVED/Build/Products/Release-iphonesimulator/Saku.app"
    echo "==> Built $APP"

    # Copy to release directory with versioned name
    echo "==> Copying to $RELEASE_ROOT/$APP_NAME"
    rm -rf "$RELEASE_ROOT/$APP_NAME"
    cp -R "$APP" "$RELEASE_ROOT/$APP_NAME"
    echo "==> Release app copied to $RELEASE_ROOT/$APP_NAME"
    echo "    Install on simulator: xcrun simctl install booted '$RELEASE_ROOT/$APP_NAME'"
    ;;

  device)
    echo "==> Building Release for iOS Device"
    echo "    Note: Requires valid code signing configuration in Xcode."
    xcodebuild \
      -project "$PROJECT" \
      -scheme "$SCHEME" \
      -configuration Release \
      -sdk iphoneos \
      -destination "generic/platform=iOS" \
      -derivedDataPath "$DERIVED" \
      build

    APP="$DERIVED/Build/Products/Release-iphoneos/Saku.app"
    echo "==> Built $APP"

    # Copy to release directory with versioned name
    echo "==> Copying to $RELEASE_ROOT/$APP_NAME"
    rm -rf "$RELEASE_ROOT/$APP_NAME"
    cp -R "$APP" "$RELEASE_ROOT/$APP_NAME"
    echo "==> Release app copied to $RELEASE_ROOT/$APP_NAME"
    echo "    Install on device: ios-deploy --bundle '$RELEASE_ROOT/$APP_NAME' (requires ios-deploy)"
    ;;

  archive)
    echo "==> Creating Release Archive for App Store distribution"
    echo "    Note: Requires valid code signing and provisioning profile."

    # Clean previous archive
    rm -rf "$ARCHIVE_PATH"

    xcodebuild \
      -project "$PROJECT" \
      -scheme "$SCHEME" \
      -configuration Release \
      -sdk iphoneos \
      -destination "generic/platform=iOS" \
      -derivedDataPath "$DERIVED" \
      -archivePath "$ARCHIVE_PATH" \
      archive

    if [ ! -d "$ARCHIVE_PATH" ]; then
      echo "!! Archive creation failed." >&2
      exit 1
    fi

    # Copy archive to release directory with versioned name
    ARCHIVE_NAME="Saku-v$VERSION_NAME.xcarchive"
    echo "==> Copying archive to $RELEASE_ROOT/$ARCHIVE_NAME"
    rm -rf "$RELEASE_ROOT/$ARCHIVE_NAME"
    cp -R "$ARCHIVE_PATH" "$RELEASE_ROOT/$ARCHIVE_NAME"
    echo "==> Archive copied to $RELEASE_ROOT/$ARCHIVE_NAME"
    echo ""
    echo "To export an IPA for App Store:"
    echo "  1. Open Xcode: open '$RELEASE_ROOT/$ARCHIVE_NAME'"
    echo "  2. Distribute App → App Store Connect"
    echo "  3. Or use: xcodebuild -exportArchive -archivePath '$RELEASE_ROOT/$ARCHIVE_NAME' -exportPath '$EXPORT_PATH' -exportOptionsPlist <plist>"
    ;;

  *)
    echo "Usage: $0 [simulator|device|archive]" >&2
    echo "  simulator (default): Build for iOS Simulator" >&2
    echo "  device: Build for physical iOS device (requires signing)" >&2
    echo "  archive: Create .xcarchive for App Store distribution" >&2
    exit 1
    ;;
esac
