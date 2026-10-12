#!/bin/bash
# Boots a simulator, installs Saku, launches it, and screenshots it.
#
# Usage:
#   ./iosApp/run-app.sh                      # default device
#   ./iosApp/run-app.sh "iPhone 18 Pro"      # by name
#   ./iosApp/run-app.sh 80FCD259-...         # by UDID

set -uo pipefail

# Same reason as build-app.sh: xcode-select points at CommandLineTools, which
# has no iOS SDK. Remove once the machine is fixed properly.
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer

SIMCTL="$DEVELOPER_DIR/usr/bin/simctl"
BUNDLE_ID="id.almezi.saku"
HERE="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$HERE/build/Saku.app"
DEVICE="${1:-iPhone 18 Pro}"

if [ ! -d "$APP_DIR" ]; then
  echo "No bundle at $APP_DIR. Run ./iosApp/build-app.sh first."
  exit 1
fi

echo "==> Resolving '$DEVICE'"
UDID=$("$SIMCTL" list devices available -j | DEVICE="$DEVICE" python3 -c '
import sys, json, os
want = os.environ["DEVICE"]
d = json.load(sys.stdin).get("devices", {})
for rt, devs in d.items():
    for dev in devs:
        if dev.get("isAvailable") and (dev["udid"] == want or dev["name"] == want):
            print(dev["udid"]); raise SystemExit
')

if [ -z "$UDID" ]; then echo "No available simulator matching '$DEVICE'"; exit 1; fi
echo "    udid=$UDID"

"$SIMCTL" bootstatus "$UDID" -b >/dev/null 2>&1 || "$SIMCTL" boot "$UDID" >/dev/null 2>&1

echo "==> Installing"
"$SIMCTL" uninstall "$UDID" "$BUNDLE_ID" >/dev/null 2>&1
"$SIMCTL" install "$UDID" "$APP_DIR" || exit 1

echo "==> Launching"
"$SIMCTL" launch "$UDID" "$BUNDLE_ID" 2>&1 | tail -2

echo "==> Waiting for Compose to settle"
sleep 8

CONTAINER=$("$SIMCTL" get_app_container "$UDID" "$BUNDLE_ID" data 2>/dev/null)
if [ -z "$CONTAINER" ]; then
  echo "!! App container not found. Did it crash on launch?"
  echo "   Console output follows:"
  "$SIMCTL" launch --console-pty "$UDID" "$BUNDLE_ID" 2>&1 | head -40
  exit 1
fi

echo "    data container: $CONTAINER"

echo "==> Screenshot"
mkdir -p "$HERE/build"
"$SIMCTL" io "$UDID" screenshot "$HERE/build/screenshot.png" >/dev/null 2>&1
echo "    $HERE/build/screenshot.png"
