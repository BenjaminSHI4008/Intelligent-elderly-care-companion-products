#!/usr/bin/env bash
# 小伴后端停止脚本 — 优雅退出，必要时强制终止
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="${SCRIPT_DIR}/.xiaoban-server.pid"
PORT="${SERVER_PORT:-8080}"
GRACE_SECONDS=15

stop_pid() {
  local pid=$1
  if ! kill -0 "$pid" 2>/dev/null; then
    return 1
  fi
  echo "[INFO] 发送 SIGTERM → PID=${pid}"
  kill "$pid" 2>/dev/null || true
  for _ in $(seq 1 "$GRACE_SECONDS"); do
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "[OK] 进程已优雅退出"
      return 0
    fi
    sleep 1
  done
  echo "[WARN] 优雅退出超时，强制终止 PID=${pid}"
  kill -9 "$pid" 2>/dev/null || true
  return 0
}

# 优先使用 PID 文件
if [[ -f "$PID_FILE" ]]; then
  PID=$(cat "$PID_FILE")
  if stop_pid "$PID"; then
    rm -f "$PID_FILE"
    echo "[OK] xiaoban-server 已停止"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

# 兜底：按端口查找 Java 进程
FOUND=false
if command -v lsof &>/dev/null; then
  PIDS=$(lsof -ti ":${PORT}" -sTCP:LISTEN 2>/dev/null || true)
  for pid in $PIDS; do
    FOUND=true
    stop_pid "$pid"
  done
fi

if [[ "$FOUND" == "true" ]]; then
  echo "[OK] 已停止占用端口 ${PORT} 的进程"
else
  echo "[INFO] 未发现运行中的 xiaoban-server"
fi
