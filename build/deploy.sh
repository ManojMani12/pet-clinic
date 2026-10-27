#!/usr/bin/env sh
set -eu

PORT="${1:-8080}"

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/build/run"
ARTIFACTS_DIR="$ROOT_DIR/build/artifacts"
PID_FILE="$RUN_DIR/app.pid"
LOG_FILE="$RUN_DIR/app.log"

mkdir -p "$RUN_DIR"

# Stop previous instance if PID file exists
if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "${OLD_PID:-}" ] && kill -0 "$OLD_PID" >/dev/null 2>&1; then
    echo "[deploy] Stopping previous app PID $OLD_PID..."
    kill "$OLD_PID" || true

    # Wait up to 15s
    i=0
    while kill -0 "$OLD_PID" >/dev/null 2>&1; do
      i=$((i+1))
      if [ "$i" -ge 15 ]; then
        echo "[deploy] Previous PID still running; forcing stop..."
        kill -9 "$OLD_PID" || true
        break
      fi
      sleep 1
    done
  fi
  rm -f "$PID_FILE"
fi

# Pick latest jar in build/artifacts
JAR_PATH="$(ls -1t "$ARTIFACTS_DIR"/*.jar 2>/dev/null | head -n 1 || true)"
if [ -z "${JAR_PATH:-}" ] || [ ! -f "$JAR_PATH" ]; then
  echo "[deploy] ERROR: No jar found in $ARTIFACTS_DIR. Run build/build.sh first." >&2
  exit 1
fi

echo "[deploy] Starting $JAR_PATH on port $PORT..."
: > "$LOG_FILE"

nohup java -jar "$JAR_PATH" --server.port="$PORT" >"$LOG_FILE" 2>&1 &
NEW_PID="$!"
echo "$NEW_PID" > "$PID_FILE"

echo "[deploy] PID: $NEW_PID"
echo "[deploy] Logs: $LOG_FILE"

# Health polling: prefer /actuator/health, fallback to /
BASE_URL="http://localhost:$PORT"
HEALTH1="$BASE_URL/actuator/health"
HEALTH2="$BASE_URL/"

echo "[deploy] Waiting for app to become healthy (timeout 90s)..."
t=0
ok=0
while [ "$t" -lt 90 ]; do
  if command -v curl >/dev/null 2>&1; then
    code="$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH1" || true)"
    if [ "$code" = "200" ]; then ok=1; break; fi
    code="$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH2" || true)"
    if [ "$code" = "200" ]; then ok=1; break; fi
  else
    # If curl not available, just check process is alive while waiting
    if ! kill -0 "$NEW_PID" >/dev/null 2>&1; then
      echo "[deploy] ERROR: App process exited early. See logs: $LOG_FILE" >&2
      exit 1
    fi
  fi

  if ! kill -0 "$NEW_PID" >/dev/null 2>&1; then
    echo "[deploy] ERROR: App process exited early. Last 50 log lines:" >&2
    tail -n 50 "$LOG_FILE" >&2 || true
    exit 1
  fi

  sleep 1
  t=$((t+1))
done

if [ "$ok" -ne 1 ]; then
  echo "[deploy] ERROR: App did not become healthy within 90s. Last 50 log lines:" >&2
  tail -n 50 "$LOG_FILE" >&2 || true
  # Stop failed run
  if kill -0 "$NEW_PID" >/dev/null 2>&1; then kill "$NEW_PID" || true; fi
  rm -f "$PID_FILE"
  exit 1
fi

echo "[deploy] Healthy at $BASE_URL"
