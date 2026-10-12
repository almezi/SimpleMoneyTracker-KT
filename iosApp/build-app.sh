#!/bin/bash
# Builds an iOS .app bundle for Saku without an Xcode project.
#
# Why no .xcodeproj yet: the shared module builds, but the framework link step
# needs the iOS SDK, and the only working SDK selection on this machine is the
# DEVELOPER_DIR override below. An Xcode project would hide that, so the build
# is kept explicit until `xcode-select` is fixed properly. See docs/09 Stage 6.
#
# Usage: ./iosApp/build-app.sh [debug|release]

set -euo pipefail

# xcode-select on this machine points at CommandLineTools, which has no iOS
# SDK, so Kotlin/Native link fails with MissingXcodeException / xcrun exit 72.
# Drop this once `sudo xcode-select --switch /Applications/Xcode.app` is run.
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer

CONFIG="${1:-debug}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

SIMCTL="$DEVELOPER_DIR/usr/bin/simctl"
FW_DIR="$ROOT/composeApp/build/bin/iosSimulatorArm64/${CONFIG}Framework"
APP_DIR="$ROOT/iosApp/build/Saku.app"

if [ ! -d "$FW_DIR/ComposeApp.framework" ]; then
  echo "Framework not built. Run:"
  echo "  ./gradlew :composeApp:link${CONFIG^}FrameworkIosSimulatorArm64"
  exit 1
fi

rm -rf "$APP_DIR"
mkdir -p "$APP_DIR"

echo "==> Compiling the Swift host against the simulator SDK"
# iOS 15 is the floor: Kotlin 2.4.20's native runtime requires it.
xcrun -sdk iphonesimulator swiftc \
  -target arm64-apple-ios15.0-simulator \
  -module-name Saku \
  -F "$FW_DIR" \
  -framework ComposeApp \
  -o "$APP_DIR/Saku" \
  "$ROOT/iosApp/SakuApp.swift"

echo "==> Assembling the bundle"
cp "$ROOT/iosApp/Info.plist" "$APP_DIR/Info.plist"

# Compose Multiplatform reads strings and fonts from a `compose-resources`
# directory at the BUNDLE ROOT at runtime. A normal Xcode build copies this in
# as a build phase; this hand-built bundle has no such phase, so copy it
# manually. Skipping this crashes on first composition with
#   MissingResourceException: Missing resource with path: .../compose-resources/...
RES_SRC="$ROOT/composeApp/build/bin/iosSimulatorArm64/debugTest/compose-resources"
if [ -d "$RES_SRC" ]; then
  echo "==> Copying compose-resources into the bundle"
  cp -R "$RES_SRC" "$APP_DIR/compose-resources"
else
  echo "!! compose-resources not found at $RES_SRC"
  echo "   The framework must be linked with the resources task running."
  exit 1
fi

# The framework is NOT copied into the bundle. It is a STATIC framework
# (isStatic = true), so its binary is an archive rather than a dylib and is
# already linked into Saku at compile time. Embedding it makes the installer
# fail with "Failed to iterate mach-o slices for input file".

echo "==> Signing (ad-hoc)"
codesign --force --sign - --timestamp=none "$APP_DIR" 2>&1 | tail -2

echo "==> Bundle built at $APP_DIR"
echo "    Run it with: ./iosApp/run-app.sh"
