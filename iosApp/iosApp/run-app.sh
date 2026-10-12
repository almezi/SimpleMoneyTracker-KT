#!/bin/bash
# STAGE 1 SPIKE — throwaway. Boots a simulator, installs, runs, captures output.
#
# Accepts either a simulator name ("iPhone 18 Pro") or a UDID
# ("80FCD259-BA75-4018-8FE6-31F4E812AD9B").

set -uo pipefail
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
SIMCTL="/Applications/Xcode.app/Contents/Developer/usr/bin/simctl"
BUNDLE_ID="spike.cmp.app"
HERE="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$HERE/build/SpikeApp.app"
DEVICE="${1:-iPhone 18 Pro}"

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

echo "==> Waiting for the app to compose and write its report"
sleep 14

CONTAINER=$("$SIMCTL" get_app_container "$UDID" "$BUNDLE_ID" data 2>/dev/null)
if [ -z "$CONTAINER" ]; then
  echo "App container not found — did it crash on launch?"
  "$SIMCTL" launch --console-pty "$UDID" "$BUNDLE_ID" 2>&1 | head -40
  exit 1
fi

REPORT="$CONTAINER/Documents/spike-report.txt"
if [ -f "$REPORT" ]; then
  echo
  echo "================= SPIKE REPORT ================="
  cat "$REPORT"
  echo "================================================"
  cp "$REPORT" /tmp/spike-report.txt
  echo "Saved to /tmp/spike-report.txt"
else
  echo "No report written at $REPORT"
  exit 1
fi

echo
echo "==> Screenshot"
"$SIMCTL" io "$UDID" screenshot "$HERE/build/screenshot.png" >/dev/null 2>&1
echo "    $HERE/build/screenshot.png"