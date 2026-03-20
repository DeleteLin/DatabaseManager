#!/usr/bin/env bash
# Android Release 编译 + USB 安装脚本（带混淆/签名）
# 用法: ./scripts/android/install-android-release.sh [选项]
# 选项:
#   -c, --clean    清理后重新构建
#   -r, --run      安装后启动应用
#   -h, --help     显示帮助信息

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
APK_PATH="${PROJECT_ROOT}/app/build/outputs/apk/release/app-release.apk"
PACKAGE_NAME="space.xiaoxiao.databasemanager"
KEYSTORE_PROPS="${PROJECT_ROOT}/keystore/keystore.properties"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

show_help() {
  echo "Android Release 编译 + USB 安装脚本"
  echo ""
  echo "用法: $0 [选项]"
  echo ""
  echo "选项:"
  echo "  -c, --clean    清理后重新构建"
  echo "  -r, --run      安装后启动应用"
  echo "  -h, --help     显示帮助信息"
  echo ""
  echo "示例:"
  echo "  $0              # 增量构建并安装 release"
  echo "  $0 -c           # 清理后构建并安装 release"
  echo "  $0 -r           # 构建安装并启动应用"
  echo "  $0 -c -r        # 清理构建安装并启动"
}

# 解析参数
CLEAN_BUILD=false
RUN_APP=false

while [[ $# -gt 0 ]]; do
  case $1 in
    -c|--clean) CLEAN_BUILD=true; shift ;;
    -r|--run) RUN_APP=true; shift ;;
    -h|--help) show_help; exit 0 ;;
    *) print_error "未知选项: $1"; show_help; exit 1 ;;
  esac
done

# 检查 adb
if ! command -v adb >/dev/null 2>&1; then
  print_error "未找到 adb。请安装 Android Platform Tools 并确保 adb 在 PATH 中。"
  exit 1
fi

# 检查 keystore
if [[ ! -f "${KEYSTORE_PROPS}" ]]; then
  print_error "缺少签名配置: ${KEYSTORE_PROPS}"
  print_info "先生成: scripts/android/generate-release-keystore.sh"
  exit 1
fi

cd "${PROJECT_ROOT}"

# 清理构建
if [[ "${CLEAN_BUILD}" == "true" ]]; then
  print_info "清理构建..."
  ./gradlew clean
fi

# 检查连接的设备（只取 device 状态）
DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" {c++} END {print c+0}')"
if [[ "${DEVICE_COUNT}" -eq 0 ]]; then
  print_error "未检测到已连接的 Android 设备"
  print_info "请确保: 1) USB 连接 2) 已启用 USB 调试 3) 已授权此电脑"
  print_info "当前 adb devices 输出："
  adb devices
  exit 1
fi

TARGET_SERIAL=""
if [[ "${DEVICE_COUNT}" -gt 1 ]]; then
  print_warning "检测到多个设备，将选择第一个 device 状态的设备。"
fi
TARGET_SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
print_info "使用设备: ${TARGET_SERIAL}"

# 构建 Release APK
print_info "构建 Release APK（含混淆/资源压缩/签名）..."
START_TIME="$(date +%s)"
./gradlew :app:assembleRelease
END_TIME="$(date +%s)"
print_success "构建完成 (耗时: $((END_TIME - START_TIME)) 秒)"

# 检查 APK 是否存在
if [[ ! -f "${APK_PATH}" ]]; then
  print_error "APK 文件不存在: ${APK_PATH}"
  print_info "你可以检查: app/build/outputs/apk/release/"
  exit 1
fi

# 安装 APK
print_info "安装 Release APK..."
adb -s "${TARGET_SERIAL}" install -r -d "${APK_PATH}"
print_success "APK 安装成功!"

# 启动应用
if [[ "${RUN_APP}" == "true" ]]; then
  print_info "启动应用..."
  adb -s "${TARGET_SERIAL}" shell monkey -p "${PACKAGE_NAME}" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
  print_success "应用已启动"
fi

echo ""
print_success "全部完成!"
if [[ "${RUN_APP}" == "false" ]]; then
  print_info "手动启动: adb -s ${TARGET_SERIAL} shell monkey -p ${PACKAGE_NAME} -c android.intent.category.LAUNCHER 1"
fi

