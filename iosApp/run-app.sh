#!/bin/bash
# Boots a simulator, installs Saku, launches it, and screenshots it.
#
# Usage:
#   ./iosApp/run-app.sh                      # default device, existing build
#   ./iosApp/run-app.sh "iPhone 18 Pro"      # by name
#   ./iosApp/run-app.sh 80FCD259-...         # by UDID
#   ./iosApp/run-app.sh --build              # build first, then run

set -uo pipefail

# See build-app.sh: CommandLineTools has no iOS SDK.
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer

SIMCTL="$DEVELOPER_DIR/usr/bin/simctl"
BUNDLE_ID="id.almezi.saku"
HERE="$(cd "$(dirname "$0")" && pwd)"
CONFIG="Debug"
APP="$HERE/build/DerivedData/Build/Products/$CONFIG-iphonesimulator/Saku.app"

DO_BUILD=0
if [ "${1:-}" = "--build" ]; then
  DO_BUILD=1
  shift
fi
DEVICE="${1:-iPhone 18 Pro}"

if [ "$DO_BUILD" = "1" ] || [ ! -d "$APP" ]; then
  "$HERE/build-app.sh" "$CONFIG" "$DEVICE" || exit 1
fi

echo "==> Resolving '$DEVICE'"
UDID=$("$SIMCTL" list devices available -j | DEVICE="$DEVICE" python3 -c '
import sys, json, os
want = os.environ["DEVICE"]
d = json.load(sys.stdin).get("devices", {})
for runtime, devices in d.items():
    for dev in devices:
        if dev.get("isAvailable") and (dev["udid"] == want or dev["name"] == want):
            print(dev["udid"]); raise SystemExit
')

if [ -z "$UDID" ]; then
  echo "No available simulator matching '$DEVICE'" >&2
  echo "Available:" >&2
  "$SIMCTL" list devices available | grep -E "iPhone|iPad" | head -10 >&2
  exit 1
fi
echo "    udid=$UDID"

"$SIMCTL" bootstatus "$UDID" -b >/dev/null 2>&1 || "$SIMCTL" boot "$UDID" >/dev/null 2>&1

echo "==> Installing"
"$SIMCTL" uninstall "$UDID" "$BUNDLE_ID" >/dev/null 2>&1
"$SIMCTL" install "$UDID" "$APP" || exit 1

echo "==> Launching"
"$SIMCTL" launch "$UDID" "$BUNDLE_ID" 2>&1 | tail -2

echo "==> Waiting for Compose to settle"
sleep 10

CONTAINER=$("$SIMCTL" get_app_container "$UDID" "$BUNDLE_ID" data 2>/dev/null)
if [ -z "$CONTAINER" ]; then
  echo "!! App container not found. Did it crash on launch?" >&2
  echo "   Console output follows:" >&2
  "$SIMCTL" launch --console-pty "$UDID" "$BUNDLE_ID" 2>&1 | head -40 >&2
  exit 1
fi

DB="$CONTAINER/Documents/simplemoneytracker.db"
echo "    data container: $CONTAINER"
if [ -f "$DB" ]; then
  echo "    ✅ database created (Room opened and seeded): $DB"
else
  echo "    ⚠️  no database at $DB — the app launched but did not open the DB"
fi

echo "==> Screenshot"
mkdir -p "$HERE/build"
"$SIMCTL" io "$UDID" screenshot "$HERE/build/screenshot.png" >/dev/null 2>&1
echo "    $HERE/build/screenshot.png"
