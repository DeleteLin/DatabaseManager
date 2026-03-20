#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
KEYSTORE_DIR="${ROOT_DIR}/keystore"
PROPS_FILE="${KEYSTORE_DIR}/keystore.properties"
KEYSTORE_FILE="${KEYSTORE_DIR}/release.jks"

mkdir -p "${KEYSTORE_DIR}"

if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: keytool not found. Please install a JDK (e.g. Temurin/Oracle) and ensure keytool is in PATH." >&2
  exit 1
fi

read -r -p "Key alias [release]: " KEY_ALIAS
KEY_ALIAS="${KEY_ALIAS:-release}"

read -r -s -p "Keystore password (min 6 chars): " STORE_PASSWORD
echo
read -r -s -p "Key password (ENTER to reuse keystore password): " KEY_PASSWORD
echo
KEY_PASSWORD="${KEY_PASSWORD:-$STORE_PASSWORD}"

read -r -p "Your name (CN) [database-manager]: " CN
CN="${CN:-database-manager}"
read -r -p "Org unit (OU) [dev]: " OU
OU="${OU:-dev}"
read -r -p "Organization (O) [xiaoxiao]: " O
O="${O:-xiaoxiao}"
read -r -p "City/Locality (L) [Unknown]: " L
L="${L:-Unknown}"
read -r -p "State/Province (ST) [Unknown]: " ST
ST="${ST:-Unknown}"
read -r -p "Country code (C) [CN]: " C
C="${C:-CN}"

DNAME="CN=${CN}, OU=${OU}, O=${O}, L=${L}, ST=${ST}, C=${C}"

if [[ -f "${KEYSTORE_FILE}" ]]; then
  echo "ERROR: ${KEYSTORE_FILE} already exists. Refusing to overwrite." >&2
  echo "If you really want a new one, delete it manually first." >&2
  exit 1
fi

keytool -genkeypair \
  -alias "${KEY_ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore "${KEYSTORE_FILE}" \
  -storepass "${STORE_PASSWORD}" \
  -keypass "${KEY_PASSWORD}" \
  -dname "${DNAME}"

cat > "${PROPS_FILE}" <<EOF
storeFile=keystore/release.jks
storePassword=${STORE_PASSWORD}
keyAlias=${KEY_ALIAS}
keyPassword=${KEY_PASSWORD}
EOF

chmod 600 "${PROPS_FILE}"

echo "OK: generated keystore at: ${KEYSTORE_FILE}"
echo "OK: wrote properties at: ${PROPS_FILE}"
echo "Next: ./gradlew :app:assembleRelease  (APK) or ./gradlew :app:bundleRelease (AAB)"
