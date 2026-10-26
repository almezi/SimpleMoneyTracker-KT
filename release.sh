#!/usr/bin/env bash
#
# release.sh — one-step release for SimpleMoneyTracker-KT.
#
# Bumps versionCode/versionName in app/build.gradle.kts, builds the unsigned
# release APK via :app:copyReleaseApk (dropped in release/ with a versioned
# name), and refreshes the README "Download APK" section: download link,
# version row, size, SHA-256, built-on date, branch, SDKs, plus moving the
# superseded APK into the previous-artifacts table.
#
# Usage:
#   ./release.sh [version] [options]
#
#     version     Version name to release, e.g. 2.4. Default: current patch
#                 component + 1 (2.3 -> 2.4). versionCode increments only when
#                 the version name actually changes. Re-running with the same
#                 version rebuilds and re-refreshes the docs (recovery path).
#   -y, --yes     Skip the confirmation prompt.
#   --no-build    Skip Gradle and assume release/SimpleMoneyTrackerKT-v<version>.apk
#                 already exists; only refreshes the docs.
#   -h, --help    Show this help.
#
# The "previous release" that gets moved into the README history table is read
# from the README's own download section, so a re-run after a partial failure
# (gradle bumped, README not yet updated) finishes the job instead of
# double-bumping. Left for human editing on purpose: CHANGELOG.md and the prose
# note under the README download table. The APK stays unsigned (no signing
# config), per README.md "Download APK".
set -euo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_PATH="$REPO/$(basename "${BASH_SOURCE[0]}")"
cd "$REPO"

GRADLE_FILE="app/build.gradle.kts"
README_FILE="README.md"
RELEASE_DIR="release"

ASSUME_YES=0
NO_BUILD=0
VERSION_ARG=""

usage() { awk 'NR == 1 {next} NR > 1 && !/^#/ {exit} {sub(/^# ?/, ""); print}' "$SCRIPT_PATH"; }

while [ $# -gt 0 ]; do
  case "$1" in
    -y|--yes) ASSUME_YES=1 ;;
    --no-build) NO_BUILD=1 ;;
    -h|--help) usage; exit 0 ;;
    -*) echo "Unknown option: $1 (see --help)" >&2; exit 2 ;;
    *) VERSION_ARG="$1" ;;
  esac
  shift
done

command -v python3 >/dev/null 2>&1 || { echo "python3 is required." >&2; exit 1; }
command -v shasum >/dev/null 2>&1 || { echo "shasum is required." >&2; exit 1; }

# --- current versions -------------------------------------------------------
# Gradle holds the version the NEXT build will carry; the README holds the
# release that is currently published. They are the same in the normal flow.
GRADLE_CODE="$(sed -n 's/^        versionCode = \([0-9]\{1,\}\)$/\1/p' "$GRADLE_FILE")"
GRADLE_NAME="$(sed -n 's/^        versionName = "\([^"]*\)"$/\1/p' "$GRADLE_FILE")"
MIN_SDK="$(sed -n 's/^        minSdk = \([0-9]\{1,\}\)$/\1/p' "$GRADLE_FILE")"
TGT_SDK="$(sed -n 's/^        targetSdk = \([0-9]\{1,\}\)$/\1/p' "$GRADLE_FILE")"
[ -n "$GRADLE_CODE" ] && [ -n "$GRADLE_NAME" ] || {
  echo "Could not parse versionCode/versionName from $GRADLE_FILE." >&2; exit 1;
}

# The published version + SHA-256 come from the README's download section.
# Parsed with python3: the row mixes literal parens with regex groups, which
# BSD sed's regex engine rejects.
PUB="$(python3 - "$README_FILE" <<'PYV'
import re
import sys

text = open(sys.argv[1], encoding="utf-8").read()
m = re.search(r"^\| Version \| ([0-9.]+) \(`versionCode` (\d+)\) \|$", text, re.M)
s = re.search(r"^\| SHA-256 \| `([0-9a-f]{64})` \|$", text, re.M)
if not (m and s):
    sys.exit(1)
print(m.group(1), m.group(2), s.group(1))
PYV
)"
if [ -z "$PUB" ]; then
  echo "Could not parse the published version/SHA-256 from $README_FILE." >&2
  echo "The download section has drifted from the expected shape; update it by hand first." >&2
  exit 1
fi
read -r PUBLISHED_NAME PUBLISHED_CODE PUBLISHED_SHA <<<"$PUB"
if [ "$GRADLE_NAME" != "$PUBLISHED_NAME" ]; then
  echo "Note: gradle is at $GRADLE_NAME but README documents $PUBLISHED_NAME as published."
  echo "      The README will be updated relative to $PUBLISHED_NAME."
fi

# --- target version ---------------------------------------------------------
if [ -n "$VERSION_ARG" ]; then
  [[ "$VERSION_ARG" =~ ^[0-9]+(\.[0-9]+)*$ ]] || {
    echo "Invalid version '$VERSION_ARG' (expected e.g. 2.4)." >&2; exit 1;
  }
  NEW_NAME="$VERSION_ARG"
else
  NEW_NAME="$(printf '%s\n' "$GRADLE_NAME" | awk -F. -v OFS=. '{$NF=$NF+1; print}')"
fi

if [ "$NEW_NAME" = "$GRADLE_NAME" ]; then
  NEW_CODE="$GRADLE_CODE"   # same version: rebuild / finish docs, code unchanged
else
  NEW_CODE=$((GRADLE_CODE + 1))
fi

TODAY="$(date +%F)"
BRANCH="$(git -C "$REPO" rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"

NEW_APK="$RELEASE_DIR/SimpleMoneyTrackerKT-v$NEW_NAME.apk"
PUBLISHED_APK="$RELEASE_DIR/SimpleMoneyTrackerKT-v$PUBLISHED_NAME.apk"

echo "Release plan"
echo "  versionName : $GRADLE_NAME -> $NEW_NAME"
echo "  versionCode : $GRADLE_CODE -> $NEW_CODE"
[ "$NO_BUILD" -eq 1 ] \
  && echo "  build       : skipped (--no-build), expecting $NEW_APK" \
  || echo "  build       : ./gradlew :app:copyReleaseApk -> $NEW_APK (unsigned)"
echo "  docs        : $README_FILE download section + previous-artifacts table"
echo

if [ "$NO_BUILD" -eq 0 ] && ! git -C "$REPO" diff --quiet 2>/dev/null; then
  echo "Note: the working tree has uncommitted changes; they will be part of this APK."
  echo
fi

if [ "$ASSUME_YES" -ne 1 ]; then
  if [ ! -t 0 ]; then
    echo "Non-interactive shell: pass --yes to proceed." >&2
    exit 1
  fi
  printf 'Proceed? [y/N] '
  read -r reply
  case "$reply" in
    y|Y|yes|Yes) ;;
    *) echo "Aborted."; exit 1 ;;
  esac
fi

BACKUP="$(mktemp -d)"
trap 'rm -rf "$BACKUP"' EXIT
cp "$GRADLE_FILE" "$BACKUP/"
restore() { cp "$BACKUP/$1" "$1"; }

# --- 1. bump the version (skipped when re-releasing the same version) -------
if [ "$NEW_NAME" != "$GRADLE_NAME" ]; then
  sed -i.bak \
    -e "s/^        versionCode = $GRADLE_CODE\$/        versionCode = $NEW_CODE/" \
    -e "s/^        versionName = \"$GRADLE_NAME\"\$/        versionName = \"$NEW_NAME\"/" \
    -e "s/?: \"$GRADLE_NAME\"/?: \"$NEW_NAME\"/" \
    "$GRADLE_FILE"
  rm -f "$GRADLE_FILE.bak"

  ok=1
  grep -q "^        versionCode = $NEW_CODE\$" "$GRADLE_FILE" \
    || { echo "ERROR: versionCode bump did not apply." >&2; ok=0; }
  grep -q "^        versionName = \"$NEW_NAME\"\$" "$GRADLE_FILE" \
    || { echo "ERROR: versionName bump did not apply." >&2; ok=0; }
  grep -q "?: \"$NEW_NAME\"" "$GRADLE_FILE" \
    || { echo "ERROR: versionName fallback did not apply." >&2; ok=0; }
  if [ "$ok" -ne 1 ]; then
    restore "$GRADLE_FILE"
    echo "$GRADLE_FILE restored; nothing was built." >&2
    exit 1
  fi
  echo "Version bumped in $GRADLE_FILE."
else
  echo "$GRADLE_FILE already at $NEW_NAME; versionCode stays $NEW_CODE."
fi

# --- 2. build + copy the versioned APK -------------------------------------
if [ "$NO_BUILD" -eq 1 ]; then
  echo "Skipping Gradle build (--no-build)."
else
  ./gradlew :app:copyReleaseApk
fi
[ -f "$NEW_APK" ] || {
  echo "ERROR: expected APK not found: $NEW_APK" >&2
  exit 1
}

SIZE="$(stat -f%z "$NEW_APK" 2>/dev/null || stat -c%s "$NEW_APK")"
NEW_SHA="$(shasum -a 256 "$NEW_APK" | awk '{print $1}')"
MIB="$(awk -v b="$SIZE" 'BEGIN{printf "%.2f", b/1048576}')"
MB="$(awk -v b="$SIZE" 'BEGIN{printf "%.2f", b/1000000}')"

# Cross-check the published APK against the checksum the README records for it.
if [ -f "$PUBLISHED_APK" ]; then
  PUBLISHED_ACTUAL="$(shasum -a 256 "$PUBLISHED_APK" | awk '{print $1}')"
  if [ "$PUBLISHED_ACTUAL" != "$PUBLISHED_SHA" ]; then
    echo "WARNING: $PUBLISHED_APK on disk does not match the README's recorded"
    echo "         SHA-256; the history row will reuse the README's checksum."
  fi
fi

# --- 3. refresh the README download section ---------------------------------
export RL_PUBLISHED_NAME="$PUBLISHED_NAME" RL_PUBLISHED_CODE="$PUBLISHED_CODE"
export RL_PUBLISHED_SHA="$PUBLISHED_SHA"
export RL_NEW_NAME="$NEW_NAME" RL_NEW_CODE="$NEW_CODE" RL_NEW_SHA="$NEW_SHA"
export RL_MIB="$MIB" RL_MB="$MB" RL_TODAY="$TODAY" RL_BRANCH="$BRANCH"
export RL_MIN_SDK="$MIN_SDK" RL_TGT_SDK="$TGT_SDK"

if ! python3 <<'PYEOF'
import os
import re
import sys

with open("README.md", encoding="utf-8") as f:
    text = f.read()

published_name = os.environ["RL_PUBLISHED_NAME"]
published_code = os.environ["RL_PUBLISHED_CODE"]
published_sha = os.environ["RL_PUBLISHED_SHA"]
new_name = os.environ["RL_NEW_NAME"]
new_code = os.environ["RL_NEW_CODE"]
new_sha = os.environ["RL_NEW_SHA"]
mib = os.environ["RL_MIB"]
mb = os.environ["RL_MB"]
today = os.environ["RL_TODAY"]
branch = os.environ["RL_BRANCH"]
min_sdk = os.environ["RL_MIN_SDK"]
tgt_sdk = os.environ["RL_TGT_SDK"]

published_file = f"SimpleMoneyTrackerKT-v{published_name}.apk"
new_file = f"SimpleMoneyTrackerKT-v{new_name}.apk"
fails = []


def sub(pattern, repl, minimum=1, label=""):
    """Apply re.subn to the whole README; record a failure instead of writing
    when the number of matches is below the minimum, so a README that drifted
    from the expected shape aborts the release rather than corrupting."""
    global text
    new_text, n = re.subn(pattern, repl, text)
    if n < minimum:
        fails.append(f"{label or pattern}: {n} match(es), expected >= {minimum}")
    else:
        text = new_text


# Every reference to the published APK filename (download link label + path,
# the hand-rebuild cp/shasum commands) now points at the new one.
sub(re.escape(published_file), new_file, 1, "published APK filename references")

sub(r"\| Version \| [0-9.]+ \(`versionCode` \d+\) \|",
    f"| Version | {new_name} (`versionCode` {new_code}) |", 1, "version row")
sub(r"\| Size \| [^\n]* \|",
    f"| Size | {mib} MiB ({mb} MB) |", 1, "size row")
sub(r"\| SHA-256 \| `[0-9a-f]{64}` \|",
    f"| SHA-256 | `{new_sha}` |", 1, "SHA-256 row")
sub(r"\| Built on \| \d{4}-\d{2}-\d{2} \|",
    f"| Built on | {today} |", 1, "built-on row")
sub(r"\| Branch \| `[^`\n]*` \|",
    f"| Branch | `{branch}` |", 1, "branch row")
sub(r"\| Min / target SDK \| \d+ / \d+ \|",
    f"| Min / target SDK | {min_sdk} / {tgt_sdk} |", 1, "SDK row")

# Move the superseded APK into the previous-artifacts table (descending order,
# so the new row goes directly under the header). Skipped on a docs-only
# re-run where published == new, and when the row is already recorded.
if published_name != new_name:
    row = (f"| `release/{published_file}` | {published_name} "
           f"(`versionCode` {published_code}) | `{published_sha}` |\n")
    header = "| File | Version | SHA-256 |\n|---|---|---|\n"
    if f"release/{published_file}`" in text:
        pass
    elif header in text:
        text = text.replace(header, header + row, 1)
    else:
        fails.append("previous-artifacts table header not found")

if fails:
    sys.stderr.write("README update FAILED, file left unmodified:\n")
    for f_ in fails:
        sys.stderr.write(f"  - {f_}\n")
    sys.exit(1)

with open("README.md", "w", encoding="utf-8") as f:
    f.write(text)
print("README.md download section updated.")
PYEOF
then
  echo
  echo "The version bump and APK are in place; only the README refresh failed."
  echo "Fix README.md, then re-run:  ./release.sh $NEW_NAME --no-build -y"
  exit 1
fi

# --- done -------------------------------------------------------------------
echo
echo "Release ready:"
echo "  APK     : $NEW_APK"
echo "  Size    : $MIB MiB ($MB MB)"
echo "  SHA-256 : $NEW_SHA"
echo
echo "Left for you (human-owned):"
echo "  - CHANGELOG.md entry for $NEW_NAME"
echo "  - prose note under the README download table (still describes the v$PUBLISHED_NAME rebuild)"
echo "  - commit the bump + APK + docs, then tag: git tag v$NEW_NAME"
