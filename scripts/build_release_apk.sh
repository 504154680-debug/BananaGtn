#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

JDK17_HOME="$HOME/.local/share/mise/installs/java/17.0.2"
if [ -d "$JDK17_HOME" ]; then
  export JAVA_HOME="$JDK17_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "[release] JAVA_HOME=${JAVA_HOME:-<unset>}"

: "${KEYSTORE_PATH:?Please set KEYSTORE_PATH}"
: "${KEYSTORE_PASSWORD:?Please set KEYSTORE_PASSWORD}"
: "${KEY_ALIAS:?Please set KEY_ALIAS}"
: "${KEY_PASSWORD:?Please set KEY_PASSWORD}"

cat > app/signing.properties <<PROPS
storeFile=${KEYSTORE_PATH}
storePassword=${KEYSTORE_PASSWORD}
keyAlias=${KEY_ALIAS}
keyPassword=${KEY_PASSWORD}
PROPS

echo "[release] signing.properties generated at app/signing.properties"
gradle :app:assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
  echo "[release] APK generated: $APK_PATH"
else
  echo "[release] APK not found: $APK_PATH"
  exit 1
fi

if command -v apksigner >/dev/null 2>&1; then
  apksigner verify --print-certs "$APK_PATH"
  echo "[release] apksigner verification completed"
else
  echo "[release] apksigner not found; skip signature verification"
fi

rm -f app/signing.properties
echo "[release] cleaned app/signing.properties"
