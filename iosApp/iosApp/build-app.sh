#!/bin/bash
# STAGE 1 SPIKE — throwaway build script.
# Compiles the Swift host, assembles a .app bundle, and links the Kotlin
# framework produced by :composeApp:linkDebugFrameworkIosSimulatorArm64.

set -euo pipefail
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FW_DIR="$ROOT/composeApp/build/bin/iosSimulatorArm64/debugFramework"
APP_DIR="$ROOT/iosApp/build/SpikeApp.app"
SIMCTL="/Applications/Xcode.app/Contents/Developer/usr/bin/simctl"

if [ ! -d "$FW_DIR/ComposeApp.framework" ]; then
  echo "Framework not built. Run:"
  echo "  ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64"
  exit 1
fi

rm -rf "$APP_DIR"
mkdir -p "$APP_DIR/Frameworks"

echo "==> Compiling Swift host against the simulator SDK"
xcrun -sdk iphonesimulator swiftc \
  -target arm64-apple-ios15.0-simulator \
  -module-name SpikeApp \
  -F "$FW_DIR" \
  -framework ComposeApp \
  -o "$APP_DIR/SpikeApp" \
  "$ROOT/iosApp/SpikeApp.swift"

echo "==> Assembling bundle"
cp "$ROOT/iosApp/Info.plist" "$APP_DIR/Info.plist"

# Compose Multiplatform reads strings and fonts from a `compose-resources`
# directory at the BUNDLE ROOT at runtime. Normally an Xcode build phase copies
# it in; this hand-built bundle has no such phase, so copy it manually.
# Skipping this crashes the app on first composition with
#   MissingResourceException: Missing resource with path: .../compose-resources/...
RES_SRC="$ROOT/composeApp/build/bin/iosSimulatorArm64/debugTest/compose-resources"
if [ -d "$RES_SRC" ]; then
  echo "==> Copying compose-resources into the bundle"
  cp -R "$RES_SRC" "$APP_DIR/compose-resources"
else
  echo "!! compose-resources not found. Run:"
  echo "   ./gradlew :composeApp:copyComposeResourcesForIosSimulatorArm64"
  exit 1
fi

# NOTE: the framework is NOT copied into the bundle. It is a STATIC framework
# (isStatic = true), so its binary is an archive, not a dylib — it is already
# linked into SpikeApp at compile time. Embedding it makes the installer fail
# with "Failed to iterate mach-o slices for input file".

echo "==> Signing (ad-hoc)"
codesign --force --sign - --timestamp=none "$APP_DIR" 2>&1 | tail -2

echo "==> Bundle built at $APP_DIR"