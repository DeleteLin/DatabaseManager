#!/bin/bash
# Android 编译安装脚本
# 用法: ./scripts/install-android.sh [选项]
# 选项:
#   -c, --clean    清理后重新构建
#   -r, --run      安装后启动应用
#   -h, --help     显示帮助信息

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="space.xiaoxiao.databasemanager"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

show_help() {
    echo "Android 编译安装脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -c, --clean    清理后重新构建"
    echo "  -r, --run      安装后启动应用"
    echo "  -h, --help     显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0              # 增量构建并安装"
    echo "  $0 -c           # 清理后构建并安装"
    echo "  $0 -r           # 构建安装并启动应用"
    echo "  $0 -c -r        # 清理构建安装并启动"
}

# 解析参数
CLEAN_BUILD=false
RUN_APP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        -r|--run)
            RUN_APP=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 检查 ANDROID_HOME
if [ -z "$ANDROID_HOME" ]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
    print_info "设置 ANDROID_HOME=$ANDROID_HOME"
fi

# 进入项目目录
cd "$PROJECT_ROOT"

# 清理构建
if [ "$CLEAN_BUILD" = true ]; then
    print_info "清理构建..."
    ./gradlew clean
fi

# 构建 APK
print_info "构建 Debug APK..."
START_TIME=$(date +%s)

./gradlew :app:assembleDebug

END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))
print_success "构建完成 (耗时: ${BUILD_TIME}秒)"

# 检查 APK 是否存在
if [ ! -f "$APK_PATH" ]; then
    print_error "APK 文件不存在: $APK_PATH"
    exit 1
fi

# 检查连接的设备
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    print_error "未检测到已连接的 Android 设备"
    print_info "请确保:"
    print_info "  1. 设备已通过 USB 连接"
    print_info "  2. 已启用 USB 调试模式"
    print_info "  3. 已授权此电脑进行调试"
    exit 1
fi

DEVICE_INFO=$(adb devices | grep "device$" | head -1 | cut -f1)
print_info "检测到设备: $DEVICE_INFO"

# 安装 APK
print_info "安装 APK..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    print_success "APK 安装成功!"
else
    print_error "APK 安装失败"
    exit 1
fi

# 启动应用
if [ "$RUN_APP" = true ]; then
    print_info "启动应用..."
    adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1
    print_success "应用已启动"
fi

echo ""
print_success "全部完成!"
if [ "$RUN_APP" = false ]; then
    print_info "手动启动: adb shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1"
fi