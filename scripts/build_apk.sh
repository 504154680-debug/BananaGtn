#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

JDK17_HOME="$HOME/.local/share/mise/installs/java/17.0.2"
if [ -d "$JDK17_HOME" ]; then
  export JAVA_HOME="$JDK17_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "[build_apk] JAVA_HOME=${JAVA_HOME:-<unset>}"
gradle :app:assembleDebug

echo "[build_apk] APK generated at:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
