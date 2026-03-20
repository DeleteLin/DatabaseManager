#!/bin/bash

# 数据库管理应用 JVM 运行脚本
# 使用方法: ./scripts/run-jvm.sh [选项]
# 选项:
#   --debug    启用调试模式
#   --clean    清理构建后运行

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 解析参数
DEBUG_MODE=false
CLEAN_BUILD=false

for arg in "$@"; do
    case $arg in
        --debug)
            DEBUG_MODE=true
            shift
            ;;
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        *)
            log_warn "未知参数: $arg"
            shift
            ;;
    esac
done

# 清理构建
if [ "$CLEAN_BUILD" = true ]; then
    log_info "清理构建..."
    ./gradlew clean
fi

# 编译检查
log_info "编译项目..."
./gradlew :app:compileKotlinJvm

if [ $? -ne 0 ]; then
    log_error "编译失败，请检查代码"
    exit 1
fi

log_info "编译成功"

# 运行应用
log_info "启动应用..."

if [ "$DEBUG_MODE" = true ]; then
    log_info "调试模式已启用"
    GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" \
    ./gradlew :app:run
else
    ./gradlew :app:run
fi