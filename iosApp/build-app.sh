#!/bin/bash
# Builds the iOS app with xcodebuild.
#
# Usage:
#   ./iosApp/build-app.sh                     # Debug, iPhone 18 Pro
#   ./iosApp/build-app.sh Release
#   ./iosApp/build-app.sh Debug "iPhone 16 Pro"
#
# The Kotlin framework is built by the "Build Kotlin framework" run-script phase
# inside the Xcode project, which calls
#   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
# That task reads its configuration from the Xcode environment, so it only works
# when driven by a real Xcode build — hand-set variables fail with
# "Could not infer iOS target platform". Do not try to replace this with a bare
# swiftc invocation; see docs/09 section 6.7.

set -euo pipefail

# xcode-select on this machine points at CommandLineTools, which ships no iOS
# SDK, so every Kotlin/Native link fails with MissingXcodeException /
# xcrun exit code 72. Remove this line once you run:
#   sudo xcode-select --switch /Applications/Xcode.app
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
unset ANDROID_PREFS_ROOT

CONFIG="${1:-Debug}"
DEVICE="${2:-iPhone 18 Pro}"
ROOT="$(cd "$(dirname "$0")" && pwd)"
DERIVED="$ROOT/build/DerivedData"
APP="$DERIVED/Build/Products/$CONFIG-iphonesimulator/Saku.app"

echo "==> Building $CONFIG for '$DEVICE'"
xcodebuild \
  -project "$ROOT/iosApp.xcodeproj" \
  -scheme Saku \
  -configuration "$CONFIG" \
  -sdk iphonesimulator \
  -destination "platform=iOS Simulator,name=$DEVICE" \
  -derivedDataPath "$DERIVED" \
  build

if [ ! -d "$APP" ]; then
  echo "!! Expected bundle at $APP but it is missing." >&2
  exit 1
fi

echo "==> Built $APP"
echo "    Run it with: ./iosApp/run-app.sh"
