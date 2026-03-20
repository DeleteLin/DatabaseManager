#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

if [[ ! -f "keystore/keystore.properties" ]]; then
  echo "Missing keystore/keystore.properties." >&2
  echo "Run: scripts/android/generate-release-keystore.sh" >&2
  exit 1
fi

./gradlew :app:assembleRelease

echo "APK output (typical): app/build/outputs/apk/release/"
