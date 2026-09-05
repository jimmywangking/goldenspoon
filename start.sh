#!/bin/bash
# CRM + 3D 模块化住房管理系统 - 一键启动脚本
# 用法: ./start.sh [start|stop|restart|status]

set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$BASE_DIR/crm-user-auth/backend"
FRONTEND_DIR="$BASE_DIR/crm-user-auth/frontend"
BACKEND_LOG="/tmp/backend.log"
FRONTEND_LOG="/tmp/frontend.log"
JAR_FILE="$BACKEND_DIR/target/crm-user-auth-1.0.0-SNAPSHOT.jar"

check_port() {
  lsof -i:$1 -sTCP:LISTEN >/dev/null 2>&1
}

get_pid() {
  lsof -i:$1 -sTCP:LISTEN -t 2>/dev/null | head -1
}

start_backend() {
  if check_port 8080; then
    echo "后端已在运行 (PID: $(get_pid 8080))"
    return
  fi

  echo "编译后端..."
  cd "$BACKEND_DIR"
  mvn clean package -DskipTests -q
  echo "启动后端..."
  nohup java -jar "$JAR_FILE" > "$BACKEND_LOG" 2>&1 &
  sleep 3
  if check_port 8080; then
    echo "后端已启动: http://localhost:8080 (PID: $(get_pid 8080))"
  else
    echo "后端启动失败，查看日志: tail -f $BACKEND_LOG"
    exit 1
  fi
}

start_frontend() {
  if check_port 5173; then
    echo "前端已在运行 (PID: $(get_pid 5173))"
    return
  fi

  echo "启动前端..."
  cd "$FRONTEND_DIR"
  nohup npm run dev > "$FRONTEND_LOG" 2>&1 &
  sleep 2
  if check_port 5173; then
    echo "前端已启动: http://localhost:5173 (PID: $(get_pid 5173))"
  else
    echo "前端启动失败，查看日志: tail -f $FRONTEND_LOG"
    exit 1
  fi
}

stop_backend() {
  if check_port 8080; then
    kill $(get_pid 8080) 2>/dev/null && echo "后端已停止"
  else
    echo "后端未运行"
  fi
}

stop_frontend() {
  if check_port 5173; then
    kill $(get_pid 5173) 2>/dev/null && echo "前端已停止"
  else
    echo "前端未运行"
  fi
}

stop_all() {
  stop_frontend
  stop_backend
}

show_status() {
  echo "=== 服务状态 ==="
  if check_port 8080; then
    echo "后端: 运行中 (PID: $(get_pid 8080)) - http://localhost:8080"
  else
    echo "后端: 未运行"
  fi
  if check_port 5173; then
    echo "前端: 运行中 (PID: $(get_pid 5173)) - http://localhost:5173"
  else
    echo "前端: 未运行"
  fi
  echo ""
  echo "=== Docker ==="
  docker ps --filter "name=pg-local" --format "PostgreSQL: {{.Status}}" 2>/dev/null || echo "PostgreSQL: 未运行"
  echo ""
  echo "=== 日志 ==="
  echo "后端日志: tail -f $BACKEND_LOG"
  echo "前端日志: tail -f $FRONTEND_LOG"
}

case "${1:-start}" in
  start)
    start_backend
    start_frontend
    echo ""
    echo "=========================================="
    echo "  CRM 系统已启动"
    echo "  前端:   http://localhost:5173"
    echo "  后端:   http://localhost:8080"
    echo "  Swagger: http://localhost:8080/swagger-ui"
    echo "=========================================="
    ;;
  stop)
    stop_all
    ;;
  restart)
    stop_all
    sleep 2
    start_backend
    start_frontend
    ;;
  status)
    show_status
    ;;
  *)
    echo "用法: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac
