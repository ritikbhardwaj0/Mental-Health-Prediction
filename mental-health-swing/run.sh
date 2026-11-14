#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PY_DIR="$PROJECT_DIR/python"
VENV="$PY_DIR/.venv"
API_PID=""

cleanup() {
  if [[ -n "$API_PID" ]] && kill -0 "$API_PID" 2>/dev/null; then
    kill "$API_PID" 2>/dev/null || true
    wait "$API_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

if ! command -v java >/dev/null || ! command -v mvn >/dev/null; then
  echo "Java 17+ and Maven are required. Install them, then run ./run.sh again." >&2
  exit 1
fi

python3 -m venv "$VENV"
"$VENV/bin/python" -m pip install --disable-pip-version-check -r "$PY_DIR/requirements.txt"
"$VENV/bin/python" "$PY_DIR/train_model.py"

cd "$PY_DIR"
"$VENV/bin/uvicorn" main:app --host 127.0.0.1 --port 8000 &
API_PID=$!

for attempt in $(seq 1 60); do
  if "$VENV/bin/python" -c 'import urllib.request; urllib.request.urlopen("http://127.0.0.1:8000/health", timeout=1)' >/dev/null 2>&1; then
    break
  fi
  if ! kill -0 "$API_PID" 2>/dev/null; then
    echo "The backend stopped during startup." >&2
    exit 1
  fi
  sleep 1
done

if ! "$VENV/bin/python" -c 'import urllib.request; urllib.request.urlopen("http://127.0.0.1:8000/health", timeout=2)' >/dev/null 2>&1; then
  echo "The backend did not become ready at http://127.0.0.1:8000." >&2
  exit 1
fi

echo "Backend ready at http://127.0.0.1:8000. Launching Swing app..."
cd "$PROJECT_DIR/java"
mvn -q compile exec:java
