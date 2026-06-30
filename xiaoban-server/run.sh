#!/usr/bin/env bash
# 小伴后端启动脚本 — 环境检查、端口检查、日志输出
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

APP_NAME="xiaoban-server"
PORT="${SERVER_PORT:-8080}"
PID_FILE="${SCRIPT_DIR}/.xiaoban-server.pid"
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/server.log"

# --- 环境检查 ---
check_command() {
  if ! command -v "$1" &>/dev/null; then
    echo "[ERROR] 未找到 $1，请先安装"
    exit 1
  fi
}

check_command java
check_command mvn

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "[INFO] Java: ${JAVA_VERSION}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "[WARN] 未设置 DB_PASSWORD，将使用 application.yml 默认值"
fi
if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "[WARN] 未设置 JWT_SECRET，生产环境请务必配置"
fi
if [[ -z "${AI_API_KEY:-}" ]]; then
  echo "[WARN] 未设置 AI_API_KEY，AI 对话功能将不可用"
fi

# --- 端口占用检查 ---
if command -v lsof &>/dev/null; then
  if lsof -i ":${PORT}" -sTCP:LISTEN &>/dev/null; then
    echo "[ERROR] 端口 ${PORT} 已被占用，请先执行 ./stop.sh 或更换 SERVER_PORT"
    exit 1
  fi
elif command -v netstat &>/dev/null; then
  if netstat -ano 2>/dev/null | grep -q ":${PORT}.*LISTENING"; then
    echo "[ERROR] 端口 ${PORT} 已被占用，请先执行 ./stop.sh"
    exit 1
  fi
fi

# --- 已在运行检查 ---
if [[ -f "$PID_FILE" ]]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "[ERROR] ${APP_NAME} 已在运行 (PID=${OLD_PID})，请先 ./stop.sh"
    exit 1
  fi
  rm -f "$PID_FILE"
fi

mkdir -p "$LOG_DIR"

echo "[INFO] 正在编译并启动 ${APP_NAME} ..."
echo "[INFO] 日志文件: ${LOG_FILE}"
echo "[INFO] Swagger:  http://localhost:${PORT}/swagger-ui.html"

nohup mvn spring-boot:run -q > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "[INFO] 启动中 (PID=$(cat "$PID_FILE"))，等待服务就绪..."
for i in $(seq 1 60); do
  if curl -sf "http://localhost:${PORT}/api-docs" >/dev/null 2>&1; then
    echo "[OK] ${APP_NAME} 已就绪 → http://localhost:${PORT}"
    exit 0
  fi
  if ! kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "[ERROR] 进程已退出，请查看日志: ${LOG_FILE}"
    tail -20 "$LOG_FILE" || true
    rm -f "$PID_FILE"
    exit 1
  fi
  sleep 2
done

echo "[WARN] 启动超时，服务可能仍在初始化，请查看: ${LOG_FILE}"
tail -10 "$LOG_FILE" || true
