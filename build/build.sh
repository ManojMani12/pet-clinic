#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ARTIFACTS_DIR="$ROOT_DIR/build/artifacts"
mkdir -p "$ARTIFACTS_DIR"

cd "$ROOT_DIR"

echo "[build] Running Maven build (tests enabled)..."
./mvnw clean package

# Try to locate the runnable jar
JAR_PATH=""
JAR_PATH_CANDIDATE="$(ls -1 target/*.jar 2>/dev/null | head -n 1 || true)"
if [ -n "${JAR_PATH_CANDIDATE:-}" ]; then
  JAR_PATH="$JAR_PATH_CANDIDATE"
fi

if [ -z "${JAR_PATH:-}" ] || [ ! -f "$JAR_PATH" ]; then
  echo "[build] ERROR: Could not find built jar under target/*.jar" >&2
  exit 1
fi

ARTIFACT_NAME="$(basename "$JAR_PATH")"
OUT_PATH="$ARTIFACTS_DIR/$ARTIFACT_NAME"
cp -f "$JAR_PATH" "$OUT_PATH"

echo "[build] Artifact: $OUT_PATH"

# SHA-256
if command -v sha256sum >/dev/null 2>&1; then
  SHA="$(sha256sum "$OUT_PATH" | awk '{print $1}')"
elif command -v shasum >/dev/null 2>&1; then
  SHA="$(shasum -a 256 "$OUT_PATH" | awk '{print $1}')"
else
  echo "[build] ERROR: sha256sum or shasum not found" >&2
  exit 1
fi

echo "[build] SHA-256: $SHA"
